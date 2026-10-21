-- Ortak sözleşme (docs/API_CONTRACT.md §3, §5): sipariş tutar kırılımı, teslim zamanı, iyimser kilit;
-- kart verisi yalnızca maskeli saklanır.
ALTER TABLE orders ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS subtotal_amount DECIMAL(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_fee DECIMAL(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS delivered_at TIMESTAMP;

UPDATE orders o
SET subtotal_amount = COALESCE((SELECT SUM(i.total_price) FROM order_items i WHERE i.order_id = o.id), o.total_amount);
UPDATE orders SET shipping_fee = GREATEST(total_amount - subtotal_amount, 0);

-- Eski kayıtlar: hash'lenmiş numaralar okunamaz → tamamen maskele; ham numaralarda son 4 hane kalır.
UPDATE payment_methods SET card_number = '**** **** **** ****'
WHERE card_number NOT LIKE '*%' AND LENGTH(card_number) > 19;
UPDATE payment_methods
SET card_number = '**** **** **** ' || RIGHT(REGEXP_REPLACE(card_number, '\D', '', 'g'), 4)
WHERE card_number NOT LIKE '*%';
UPDATE payment_methods
SET account_number = '****' || RIGHT(REGEXP_REPLACE(account_number, '\D', '', 'g'), 4)
WHERE account_number IS NOT NULL AND account_number NOT LIKE '*%';
UPDATE payment_methods SET cvv = NULL;

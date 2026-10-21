-- orders / order_items: V1 legacy columns (shipping_address TEXT) vs current JPA (FK ids + total_price)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipping_address_id BIGINT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS billing_address_id BIGINT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method_id BIGINT;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS notes TEXT;

ALTER TABLE order_items ADD COLUMN IF NOT EXISTS total_price DECIMAL(12,2);
UPDATE order_items SET total_price = quantity * unit_price WHERE total_price IS NULL;
ALTER TABLE order_items ALTER COLUMN total_price SET NOT NULL;

-- Sipariş yaşam döngüsü: kargo takip, iptal/iade notları (PostgreSQL)

ALTER TABLE orders ADD COLUMN IF NOT EXISTS tracking_number VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS carrier VARCHAR(64);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS shipped_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS estimated_delivery_at TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS return_reason VARCHAR(500);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS return_requested_at TIMESTAMP;

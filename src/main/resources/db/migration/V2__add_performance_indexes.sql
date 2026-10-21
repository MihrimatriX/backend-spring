-- V2__add_performance_indexes.sql
-- Additional performance indexes for better query performance

-- Composite indexes for common queries
CREATE INDEX idx_products_category_active ON products(category_id, is_active);
CREATE INDEX idx_products_discount_active ON products(discount, is_active) WHERE discount > 0;
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_status ON orders(created_at, status);
CREATE INDEX idx_reviews_product_rating ON reviews(product_id, rating);
CREATE INDEX idx_reviews_user_created ON reviews(user_id, created_at);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
CREATE INDEX idx_login_histories_user_login ON login_histories(user_id, login_at);
CREATE INDEX idx_addresses_user_default ON addresses(user_id, is_default);
CREATE INDEX idx_payment_methods_user_default ON payment_methods(user_id, is_default);

-- Text search indexes for product search
CREATE INDEX idx_products_name_search ON products USING gin(to_tsvector('english', product_name));
CREATE INDEX idx_products_description_search ON products USING gin(to_tsvector('english', description));

-- Partial indexes for active records
CREATE INDEX idx_products_active_discount ON products(discount) WHERE is_active = TRUE AND discount > 0;
CREATE INDEX idx_campaigns_active_dates ON campaigns(start_date, end_date) WHERE is_active = TRUE;

-- Indexes for foreign key lookups
CREATE INDEX idx_order_items_order_active ON order_items(order_id) WHERE is_active = TRUE;
CREATE INDEX idx_favorites_user_active ON favorites(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_reviews_product_active ON reviews(product_id) WHERE is_active = TRUE;

-- Indexes for date-based queries
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_products_created_at ON products(created_at DESC);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
CREATE INDEX idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX idx_login_histories_login_at ON login_histories(login_at DESC);

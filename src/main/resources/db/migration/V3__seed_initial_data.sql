-- V3__seed_initial_data.sql
-- Initial seed data for development and testing

-- Insert sample categories
INSERT INTO categories (category_name, description, image_url) VALUES
('Electronics', 'Electronic devices and gadgets', 'https://example.com/images/electronics.jpg'),
('Clothing', 'Fashion and apparel', 'https://example.com/images/clothing.jpg'),
('Books', 'Books and educational materials', 'https://example.com/images/books.jpg'),
('Home & Garden', 'Home improvement and garden supplies', 'https://example.com/images/home-garden.jpg'),
('Sports', 'Sports equipment and accessories', 'https://example.com/images/sports.jpg'),
('Beauty', 'Beauty and personal care products', 'https://example.com/images/beauty.jpg'),
('Toys', 'Toys and games for children', 'https://example.com/images/toys.jpg'),
('Automotive', 'Car parts and automotive accessories', 'https://example.com/images/automotive.jpg');

-- Insert sample products
INSERT INTO products (product_name, unit_price, unit_in_stock, quantity_per_unit, category_id, description, image_url, discount) VALUES
-- Electronics
('iPhone 15 Pro', 999.99, 50, '1 piece', 1, 'Latest iPhone with advanced camera system', 'https://example.com/images/iphone15.jpg', 0),
('Samsung Galaxy S24', 899.99, 45, '1 piece', 1, 'Premium Android smartphone', 'https://example.com/images/galaxy-s24.jpg', 10),
('MacBook Pro M3', 1999.99, 25, '1 piece', 1, 'Professional laptop for creators', 'https://example.com/images/macbook-pro.jpg', 5),
('AirPods Pro', 249.99, 100, '1 piece', 1, 'Wireless earbuds with noise cancellation', 'https://example.com/images/airpods-pro.jpg', 0),

-- Clothing
('Nike Air Max 270', 150.00, 80, '1 pair', 2, 'Comfortable running shoes', 'https://example.com/images/nike-airmax.jpg', 15),
('Levi''s 501 Jeans', 89.99, 60, '1 piece', 2, 'Classic straight-fit jeans', 'https://example.com/images/levis-501.jpg', 0),
('Adidas T-Shirt', 29.99, 120, '1 piece', 2, 'Comfortable cotton t-shirt', 'https://example.com/images/adidas-tshirt.jpg', 20),

-- Books
('Clean Code', 45.99, 30, '1 book', 3, 'A Handbook of Agile Software Craftsmanship', 'https://example.com/images/clean-code.jpg', 0),
('Design Patterns', 59.99, 25, '1 book', 3, 'Elements of Reusable Object-Oriented Software', 'https://example.com/images/design-patterns.jpg', 10),
('The Pragmatic Programmer', 39.99, 40, '1 book', 3, 'Your Journey to Mastery', 'https://example.com/images/pragmatic-programmer.jpg', 0),

-- Home & Garden
('Philips LED Bulb Set', 24.99, 200, '4 pieces', 4, 'Energy-efficient LED light bulbs', 'https://example.com/images/led-bulbs.jpg', 25),
('IKEA Coffee Table', 149.99, 15, '1 piece', 4, 'Modern wooden coffee table', 'https://example.com/images/coffee-table.jpg', 0),

-- Sports
('Yoga Mat', 34.99, 75, '1 piece', 5, 'Non-slip yoga mat for exercise', 'https://example.com/images/yoga-mat.jpg', 0),
('Dumbbell Set', 89.99, 20, '2 pieces', 5, 'Adjustable dumbbell set', 'https://example.com/images/dumbbells.jpg', 15),

-- Beauty
('L''Oreal Shampoo', 12.99, 150, '1 bottle', 6, 'Moisturizing shampoo for all hair types', 'https://example.com/images/loreal-shampoo.jpg', 0),
('Nivea Face Cream', 8.99, 100, '1 tube', 6, 'Daily moisturizing face cream', 'https://example.com/images/nivea-cream.jpg', 30),

-- Toys
('LEGO Creator Set', 79.99, 35, '1 set', 7, 'Creative building blocks for kids', 'https://example.com/images/lego-creator.jpg', 0),
('Barbie Dreamhouse', 199.99, 10, '1 piece', 7, 'Multi-story dollhouse with accessories', 'https://example.com/images/barbie-dreamhouse.jpg', 20),

-- Automotive
('Car Phone Mount', 19.99, 80, '1 piece', 8, 'Universal smartphone car mount', 'https://example.com/images/car-mount.jpg', 0),
('Car Air Freshener', 4.99, 200, '1 piece', 8, 'Long-lasting car air freshener', 'https://example.com/images/air-freshener.jpg', 40);

-- Insert sample campaigns
INSERT INTO campaigns (title, subtitle, description, discount, image_url, background_color, time_left, button_text, button_href, start_date, end_date) VALUES
('Black Friday Sale', 'Up to 70% Off', 'Huge discounts on all electronics and gadgets', 70, 'https://example.com/images/black-friday.jpg', '#FF0000', '5 days left', 'Shop Now', '/products?category=1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '7 days'),
('Summer Fashion', 'New Collection', 'Fresh summer styles at amazing prices', 50, 'https://example.com/images/summer-fashion.jpg', '#FFA500', '10 days left', 'Explore', '/products?category=2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '14 days'),
('Book Lovers Week', 'Knowledge is Power', 'Special discounts on educational books', 30, 'https://example.com/images/books-sale.jpg', '#4CAF50', '3 days left', 'Read More', '/products?category=3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '5 days');

-- Create a sample admin user (password: admin123)
INSERT INTO users (email, password, first_name, last_name, phone_number, is_email_verified, is_active) VALUES
('admin@ecommerce.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Admin', 'User', '+1234567890', TRUE, TRUE);

-- Create a sample regular user (password: user123)
INSERT INTO users (email, password, first_name, last_name, phone_number, is_email_verified, is_active) VALUES
('user@ecommerce.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'John', 'Doe', '+1234567891', TRUE, TRUE);

-- Insert sample addresses for the regular user
INSERT INTO addresses (user_id, title, full_address, city, district, postal_code, country, is_default, phone_number) VALUES
(2, 'Home', '123 Main Street, Apt 4B', 'Istanbul', 'Kadikoy', '34710', 'Turkey', TRUE, '+1234567891'),
(2, 'Work', '456 Business Avenue, Floor 10', 'Istanbul', 'Sisli', '34394', 'Turkey', FALSE, '+1234567891');

-- Insert sample payment methods for the regular user
INSERT INTO payment_methods (user_id, type, card_holder_name, card_number, expiry_month, expiry_year, cvv, is_default) VALUES
(2, 'CreditCard', 'John Doe', '**** **** **** 1234', 12, 2025, '***', TRUE),
(2, 'DebitCard', 'John Doe', '**** **** **** 5678', 10, 2026, '***', FALSE);

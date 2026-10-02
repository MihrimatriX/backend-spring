-- V13__catalog_contract.sql
-- Katalog sözleşmesi (docs/API_CONTRACT.md §4.2–§4.4): alt kategoriler ve ürün → alt kategori ilişkisi.
-- Başlangıç alt kategorileri SQL ile değil, SubCategorySeeder (ApplicationRunner) ile eklenir.

CREATE TABLE subcategories (
    id BIGSERIAL PRIMARY KEY,
    sub_category_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(255),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_subcategories_category_name UNIQUE (category_id, sub_category_name)
);

CREATE INDEX idx_subcategories_category_active ON subcategories (category_id, is_active);

ALTER TABLE products ADD COLUMN sub_category_id BIGINT REFERENCES subcategories(id);

CREATE INDEX idx_products_sub_category ON products (sub_category_id);

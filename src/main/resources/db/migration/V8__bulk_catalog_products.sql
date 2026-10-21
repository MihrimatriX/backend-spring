-- Large synthetic catalog for demos (categories 1–18 from V3+V4)

INSERT INTO products (
    product_name,
    unit_price,
    unit_in_stock,
    quantity_per_unit,
    category_id,
    description,
    image_url,
    discount,
    version,
    is_active
)
SELECT
    'Katalog ' || LPAD(n::text, 4, '0'),
    ROUND((49.99 + random() * 450)::numeric, 2),
    (5 + floor(random() * 150))::int,
    '1 adet',
    1 + ((n - 1) % 18),
    'Demo katalog ürünü #' || n || '. Fiyat ve stok örnek veridir.',
    'https://picsum.photos/seed/rsk' || n || '/400/300',
    CASE WHEN random() < 0.28 THEN (5 + floor(random() * 26))::int ELSE 0 END,
    0,
    TRUE
FROM generate_series(1, 500) AS n;

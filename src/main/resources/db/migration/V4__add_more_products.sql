-- V4__add_more_products.sql
-- Add more products for better testing

-- Insert more categories
INSERT INTO categories (category_name, description, image_url) VALUES
('Elektronik', 'Telefon, bilgisayar, tablet ve elektronik cihazlar', 'https://picsum.photos/300/200?random=1'),
('Moda', 'Giyim, ayakkabı, çanta ve aksesuar', 'https://picsum.photos/300/200?random=2'),
('Ev & Yaşam', 'Dekorasyon, mutfak, banyo ve ev eşyaları', 'https://picsum.photos/300/200?random=3'),
('Spor & Outdoor', 'Fitness, kamp, outdoor ve spor malzemeleri', 'https://picsum.photos/300/200?random=4'),
('Anne & Bebek', 'Bebek giyim, oyuncak ve bakım ürünleri', 'https://picsum.photos/300/200?random=5'),
('Kozmetik & Bakım', 'Makyaj, cilt bakımı ve kişisel bakım', 'https://picsum.photos/300/200?random=6'),
('Süpermarket', 'Gıda, temizlik ve günlük ihtiyaçlar', 'https://picsum.photos/300/200?random=7'),
('Kitap & Müzik', 'Kitaplar, müzik, film ve eğitim', 'https://picsum.photos/300/200?random=8'),
('Oto & Bahçe', 'Araç, bahçe malzemeleri ve aksesuarlar', 'https://picsum.photos/300/200?random=9'),
('Kırtasiye & Ofis', 'Ofis, kırtasiye ve iş malzemeleri', 'https://picsum.photos/300/200?random=10');

-- Insert many more products
INSERT INTO products (product_name, unit_price, unit_in_stock, quantity_per_unit, category_id, description, image_url, discount) VALUES
-- Elektronik ürünleri (Category ID: 1)
('iPhone 15 Pro Max', 1299.99, 25, '1 adet', 1, 'En yeni iPhone modeli, gelişmiş kamera sistemi', 'https://picsum.photos/300/200?random=101', 5),
('Samsung Galaxy S24 Ultra', 1199.99, 30, '1 adet', 1, 'Premium Android akıllı telefon', 'https://picsum.photos/300/200?random=102', 10),
('MacBook Air M3', 1299.99, 20, '1 adet', 1, 'Hafif ve güçlü laptop', 'https://picsum.photos/300/200?random=103', 0),
('iPad Pro 12.9"', 1099.99, 15, '1 adet', 1, 'Profesyonel tablet', 'https://picsum.photos/300/200?random=104', 8),
('Sony WH-1000XM5', 399.99, 40, '1 adet', 1, 'Gürültü önleyici kulaklık', 'https://picsum.photos/300/200?random=105', 15),
('Dell XPS 13', 999.99, 12, '1 adet', 1, 'Ultrabook laptop', 'https://picsum.photos/300/200?random=106', 0),
('Nintendo Switch OLED', 349.99, 35, '1 adet', 1, 'Taşınabilir oyun konsolu', 'https://picsum.photos/300/200?random=107', 20),
('Apple Watch Series 9', 399.99, 50, '1 adet', 1, 'Akıllı saat', 'https://picsum.photos/300/200?random=108', 0),
('Samsung 55" QLED TV', 899.99, 8, '1 adet', 1, '4K QLED televizyon', 'https://picsum.photos/300/200?random=109', 25),
('Canon EOS R6', 2499.99, 5, '1 adet', 1, 'Profesyonel fotoğraf makinesi', 'https://picsum.photos/300/200?random=110', 0),

-- Moda ürünleri (Category ID: 2)
('Nike Air Jordan 1', 120.00, 60, '1 çift', 2, 'Klasik basketbol ayakkabısı', 'https://picsum.photos/300/200?random=201', 0),
('Adidas Ultraboost 22', 180.00, 45, '1 çift', 2, 'Koşu ayakkabısı', 'https://picsum.photos/300/200?random=202', 15),
('Levi''s 511 Slim Jeans', 79.99, 80, '1 adet', 2, 'Slim fit kot pantolon', 'https://picsum.photos/300/200?random=203', 10),
('Zara Oversized T-Shirt', 19.99, 100, '1 adet', 2, 'Oversize pamuklu t-shirt', 'https://picsum.photos/300/200?random=204', 0),
('H&M Hoodie', 39.99, 70, '1 adet', 2, 'Kapüşonlu sweatshirt', 'https://picsum.photos/300/200?random=205', 20),
('Converse Chuck Taylor', 65.00, 90, '1 çift', 2, 'Klasik canvas ayakkabı', 'https://picsum.photos/300/200?random=206', 0),
('Uniqlo Heattech T-Shirt', 14.99, 120, '1 adet', 2, 'Isıtıcı iç çamaşırı', 'https://picsum.photos/300/200?random=207', 30),
('Puma Suede Classic', 75.00, 55, '1 çift', 2, 'Klasik spor ayakkabı', 'https://picsum.photos/300/200?random=208', 0),
('Tommy Hilfiger Polo', 89.99, 40, '1 adet', 2, 'Polo yaka t-shirt', 'https://picsum.photos/300/200?random=209', 25),
('Vans Old Skool', 70.00, 65, '1 çift', 2, 'Skateboard ayakkabısı', 'https://picsum.photos/300/200?random=210', 0),

-- Ev & Yaşam ürünleri (Category ID: 3)
('IKEA MALM Yatak', 199.99, 10, '1 adet', 3, 'Modern yatak çerçevesi', 'https://picsum.photos/300/200?random=301', 0),
('Philips Hue Starter Kit', 149.99, 25, '1 set', 3, 'Akıllı LED ampul seti', 'https://picsum.photos/300/200?random=302', 20),
('Dyson V15 Detect', 649.99, 8, '1 adet', 3, 'Kablosuz elektrikli süpürge', 'https://picsum.photos/300/200?random=303', 0),
('KitchenAid Stand Mixer', 399.99, 5, '1 adet', 3, 'Profesyonel mutfak mikseri', 'https://picsum.photos/300/200?random=304', 15),
('Nespresso Vertuo', 199.99, 20, '1 adet', 3, 'Kahve makinesi', 'https://picsum.photos/300/200?random=305', 10),
('Instant Pot Duo', 99.99, 30, '1 adet', 3, 'Çok fonksiyonlu düdüklü tencere', 'https://picsum.photos/300/200?random=306', 0),
('Roomba i7+', 799.99, 6, '1 adet', 3, 'Akıllı robot süpürge', 'https://picsum.photos/300/200?random=307', 25),
('Casper Memory Foam Mattress', 595.00, 12, '1 adet', 3, 'Hafızalı köpük yatak', 'https://picsum.photos/300/200?random=308', 0),
('Vitamix A3500', 499.99, 4, '1 adet', 3, 'Profesyonel blender', 'https://picsum.photos/300/200?random=309', 30),
('Echo Dot (4th Gen)', 49.99, 50, '1 adet', 3, 'Akıllı hoparlör', 'https://picsum.photos/300/200?random=310', 0),

-- Spor & Outdoor ürünleri (Category ID: 4)
('Peloton Bike', 1495.00, 3, '1 adet', 4, 'Akıllı egzersiz bisikleti', 'https://picsum.photos/300/200?random=401', 0),
('Lululemon Align Leggings', 98.00, 40, '1 adet', 4, 'Yoga taytı', 'https://picsum.photos/300/200?random=402', 0),
('Patagonia Down Jacket', 229.00, 15, '1 adet', 4, 'Kış montu', 'https://picsum.photos/300/200?random=403', 20),
('Yeti Rambler Tumbler', 35.00, 80, '1 adet', 4, 'Termos bardak', 'https://picsum.photos/300/200?random=404', 0),
('Garmin Forerunner 945', 599.99, 12, '1 adet', 4, 'GPS spor saati', 'https://picsum.photos/300/200?random=405', 15),
('Hydro Flask Water Bottle', 32.00, 100, '1 adet', 4, 'Su şişesi', 'https://picsum.photos/300/200?random=406', 0),
('Arc''teryx Beta AR Jacket', 399.00, 8, '1 adet', 4, 'Yağmurluk', 'https://picsum.photos/300/200?random=407', 25),
('Bowflex Adjustable Dumbbells', 549.00, 6, '1 çift', 4, 'Ayarlanabilir dambıl', 'https://picsum.photos/300/200?random=408', 0),
('Coleman Sundome Tent', 79.99, 20, '1 adet', 4, 'Kamp çadırı', 'https://picsum.photos/300/200?random=409', 30),
('Brooks Ghost 14', 130.00, 35, '1 çift', 4, 'Koşu ayakkabısı', 'https://picsum.photos/300/200?random=410', 0),

-- Anne & Bebek ürünleri (Category ID: 5)
('Graco 4Ever Car Seat', 299.99, 8, '1 adet', 5, 'Bebek araç koltuğu', 'https://picsum.photos/300/200?random=501', 0),
('BabyBjörn Carrier One', 199.99, 12, '1 adet', 5, 'Bebek taşıyıcısı', 'https://picsum.photos/300/200?random=502', 15),
('Fisher-Price Rock ''n Play', 149.99, 15, '1 adet', 5, 'Bebek sallama koltuğu', 'https://picsum.photos/300/200?random=503', 20),
('Huggies Little Snugglers', 39.99, 50, '1 paket', 5, 'Bebek bezi', 'https://picsum.photos/300/200?random=504', 0),
('Skip Hop Diaper Bag', 79.99, 20, '1 adet', 5, 'Bebek çantası', 'https://picsum.photos/300/200?random=505', 10),
('Philips Avent Bottle Set', 24.99, 40, '1 set', 5, 'Biberon seti', 'https://picsum.photos/300/200?random=506', 0),
('Baby Einstein Activity Table', 89.99, 18, '1 adet', 5, 'Aktivite masası', 'https://picsum.photos/300/200?random=507', 25),
('Ergobaby Omni 360', 179.99, 10, '1 adet', 5, 'Ergonomik bebek taşıyıcısı', 'https://picsum.photos/300/200?random=508', 0),
('Munchkin Latch Bottles', 19.99, 60, '1 set', 5, 'Biberon seti', 'https://picsum.photos/300/200?random=509', 30),
('Summer Infant Baby Monitor', 129.99, 12, '1 adet', 5, 'Bebek monitörü', 'https://picsum.photos/300/200?random=510', 0),

-- Kozmetik & Bakım ürünleri (Category ID: 6)
('La Mer The Moisturizing Cream', 350.00, 5, '1 kavanoz', 6, 'Lüks nemlendirici krem', 'https://picsum.photos/300/200?random=601', 0),
('SK-II Facial Treatment Essence', 199.00, 8, '1 şişe', 6, 'Cilt bakım serumu', 'https://picsum.photos/300/200?random=602', 15),
('Charlotte Tilbury Pillow Talk', 34.00, 25, '1 adet', 6, 'Ruj', 'https://picsum.photos/300/200?random=603', 0),
('Drunk Elephant C-Firma', 78.00, 12, '1 şişe', 6, 'C vitamini serumu', 'https://picsum.photos/300/200?random=604', 20),
('Fenty Beauty Pro Filt''r Foundation', 36.00, 30, '1 adet', 6, 'Fondöten', 'https://picsum.photos/300/200?random=605', 0),
('The Ordinary Niacinamide 10%', 6.80, 50, '1 şişe', 6, 'Niasinamid serumu', 'https://picsum.photos/300/200?random=606', 0),
('Glossier Boy Brow', 18.00, 40, '1 adet', 6, 'Kaş jeli', 'https://picsum.photos/300/200?random=607', 25),
('Olaplex No.3 Hair Perfector', 28.00, 35, '1 şişe', 6, 'Saç bakım ürünü', 'https://picsum.photos/300/200?random=608', 0),
('Anastasia Beverly Hills Brow Wiz', 23.00, 45, '1 adet', 6, 'Kaş kalemi', 'https://picsum.photos/300/200?random=609', 30),
('Kiehl''s Ultra Facial Cream', 32.00, 20, '1 kavanoz', 6, 'Günlük nemlendirici', 'https://picsum.photos/300/200?random=610', 0),

-- Süpermarket ürünleri (Category ID: 7)
('Organic Quinoa 1kg', 12.99, 100, '1 paket', 7, 'Organik kinoa', 'https://picsum.photos/300/200?random=701', 0),
('Coconut Oil 500ml', 8.99, 80, '1 şişe', 7, 'Hindistan cevizi yağı', 'https://picsum.photos/300/200?random=702', 15),
('Almond Milk 1L', 4.99, 120, '1 kutu', 7, 'Badem sütü', 'https://picsum.photos/300/200?random=703', 0),
('Dark Chocolate 85%', 6.99, 60, '1 tablet', 7, 'Bitter çikolata', 'https://picsum.photos/300/200?random=704', 20),
('Green Tea Bags 100pcs', 9.99, 90, '1 kutu', 7, 'Yeşil çay poşetleri', 'https://picsum.photos/300/200?random=705', 0),
('Honey 500g', 11.99, 70, '1 kavanoz', 7, 'Doğal bal', 'https://picsum.photos/300/200?random=706', 25),
('Olive Oil Extra Virgin 1L', 15.99, 50, '1 şişe', 7, 'Sızma zeytinyağı', 'https://picsum.photos/300/200?random=707', 0),
('Protein Powder 1kg', 29.99, 40, '1 kutu', 7, 'Protein tozu', 'https://picsum.photos/300/200?random=708', 30),
('Granola Mix 500g', 7.99, 85, '1 paket', 7, 'Granola karışımı', 'https://picsum.photos/300/200?random=709', 0),
('Sea Salt 1kg', 3.99, 150, '1 paket', 7, 'Deniz tuzu', 'https://picsum.photos/300/200?random=710', 0),

-- Kitap & Müzik ürünleri (Category ID: 8)
('Atomic Habits', 16.99, 30, '1 kitap', 8, 'Alışkanlıkların gücü', 'https://picsum.photos/300/200?random=801', 0),
('The 7 Habits of Highly Effective People', 18.99, 25, '1 kitap', 8, 'Etkili insanların 7 alışkanlığı', 'https://picsum.photos/300/200?random=802', 15),
('Think and Grow Rich', 14.99, 35, '1 kitap', 8, 'Düşün ve zenginleş', 'https://picsum.photos/300/200?random=803', 0),
('Rich Dad Poor Dad', 17.99, 28, '1 kitap', 8, 'Zengin baba yoksul baba', 'https://picsum.photos/300/200?random=804', 20),
('The Lean Startup', 19.99, 22, '1 kitap', 8, 'Yalın girişim', 'https://picsum.photos/300/200?random=805', 0),
('Good to Great', 16.99, 32, '1 kitap', 8, 'İyiden mükemmele', 'https://picsum.photos/300/200?random=806', 25),
('The Power of Now', 15.99, 40, '1 kitap', 8, 'Şimdinin gücü', 'https://picsum.photos/300/200?random=807', 0),
('Sapiens', 21.99, 18, '1 kitap', 8, 'Hayvanlardan tanrılara', 'https://picsum.photos/300/200?random=808', 30),
('The Subtle Art of Not Giving a F*ck', 13.99, 45, '1 kitap', 8, 'Hiçbir şeyi takmama sanatı', 'https://picsum.photos/300/200?random=809', 0),
('Educated', 17.99, 26, '1 kitap', 8, 'Eğitilmiş', 'https://picsum.photos/300/200?random=810', 0),

-- Oto & Bahçe ürünleri (Category ID: 9)
('Car Phone Mount', 24.99, 60, '1 adet', 9, 'Araç telefon tutucu', 'https://picsum.photos/300/200?random=901', 0),
('Car Air Freshener', 8.99, 100, '1 adet', 9, 'Araç hava spreyi', 'https://picsum.photos/300/200?random=902', 15),
('Garden Hose 50ft', 39.99, 25, '1 adet', 9, 'Bahçe hortumu', 'https://picsum.photos/300/200?random=903', 0),
('Plant Pot Set', 29.99, 40, '1 set', 9, 'Saksı seti', 'https://picsum.photos/300/200?random=904', 20),
('Car Vacuum Cleaner', 49.99, 15, '1 adet', 9, 'Araç elektrikli süpürgesi', 'https://picsum.photos/300/200?random=905', 0),
('Garden Tools Set', 79.99, 12, '1 set', 9, 'Bahçe aletleri seti', 'https://picsum.photos/300/200?random=906', 25),
('Car Dashboard Camera', 89.99, 8, '1 adet', 9, 'Araç kamerası', 'https://picsum.photos/300/200?random=907', 0),
('Outdoor Solar Lights', 34.99, 30, '1 set', 9, 'Güneş enerjili bahçe ışıkları', 'https://picsum.photos/300/200?random=908', 30),
('Car Seat Covers', 59.99, 20, '1 set', 9, 'Araç koltuk kılıfları', 'https://picsum.photos/300/200?random=909', 0),
('Garden Sprinkler', 19.99, 35, '1 adet', 9, 'Bahçe fıskiyesi', 'https://picsum.photos/300/200?random=910', 0),

-- Kırtasiye & Ofis ürünleri (Category ID: 10)
('Moleskine Notebook', 24.99, 50, '1 adet', 10, 'Premium defter', 'https://picsum.photos/300/200?random=1001', 0),
('Pilot G2 Pen Set', 12.99, 80, '1 set', 10, 'Kalem seti', 'https://picsum.photos/300/200?random=1002', 15),
('Staples Paper Clips', 4.99, 200, '1 kutu', 10, 'Ataş', 'https://picsum.photos/300/200?random=1003', 0),
('Post-it Notes Pack', 8.99, 120, '1 paket', 10, 'Yapışkan notlar', 'https://picsum.photos/300/200?random=1004', 20),
('Sharpie Marker Set', 15.99, 60, '1 set', 10, 'Kalıcı kalem seti', 'https://picsum.photos/300/200?random=1005', 0),
('Binder Clips Assorted', 6.99, 150, '1 kutu', 10, 'Dosya klipsleri', 'https://picsum.photos/300/200?random=1006', 25),
('Whiteboard 24x36', 29.99, 20, '1 adet', 10, 'Beyaz tahta', 'https://picsum.photos/300/200?random=1007', 0),
('Desk Organizer', 19.99, 40, '1 adet', 10, 'Masa düzenleyici', 'https://picsum.photos/300/200?random=1008', 30),
('Hole Punch', 12.99, 30, '1 adet', 10, 'Delgeç', 'https://picsum.photos/300/200?random=1009', 0),
('Calculator Scientific', 18.99, 25, '1 adet', 10, 'Bilimsel hesap makinesi', 'https://picsum.photos/300/200?random=1010', 0);

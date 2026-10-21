-- Ek demo kullanıcılar + çoklu yazarlı Türkçe yorumlar (user_id, product_id) tekilliğine uygun)
-- Şifre tümü: user123 (V3 ile aynı BCrypt)
-- admin@ecommerce.com / user@ecommerce.com aynı hash ile eklenmişti; yeni hesaplar da user123

INSERT INTO users (email, password, first_name, last_name, phone_number, address, city, postal_code, is_email_verified, is_active)
SELECT v.email, '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', v.fn, v.ln, v.phone, v.ad, v.ci, v.pc, TRUE, TRUE
FROM (
    VALUES
        ('zeynep.kaya@demo.kapidamart.local', 'Zeynep', 'Kaya', '+90 532 100 0001', 'Bahçelievler Mah.', 'İstanbul', '34180'),
        ('can.ozturk@demo.kapidamart.local', 'Can', 'Öztürk', '+90 532 100 0002', 'Çankaya Cad.', 'Ankara', '06420'),
        ('elif.yildiz@demo.kapidamart.local', 'Elif', 'Yıldız', '+90 532 100 0003', 'Alsancak', 'İzmir', '35220'),
        ('burak.arslan@demo.kapidamart.local', 'Burak', 'Arslan', '+90 532 100 0004', 'Nilüfer', 'Bursa', '16140'),
        ('selin.aktas@demo.kapidamart.local', 'Selin', 'Aktaş', '+90 532 100 0005', 'Antalya Merkez', 'Antalya', '07040'),
        ('emre.demir@demo.kapidamart.local', 'Emre', 'Demir', '+90 532 100 0006', 'Trabzon Merkez', 'Trabzon', '61030'),
        ('deniz.celik@demo.kapidamart.local', 'Deniz', 'Çelik', '+90 532 100 0007', 'Konak', 'İzmir', '35250'),
        ('melis.sahin@demo.kapidamart.local', 'Melis', 'Şahin', '+90 532 100 0008', 'Ümraniye', 'İstanbul', '34764'),
        ('kerem.polat@demo.kapidamart.local', 'Kerem', 'Polat', '+90 532 100 0009', 'Keçiören', 'Ankara', '06280'),
        ('ece.vural@demo.kapidamart.local', 'Ece', 'Vural', '+90 532 100 0010', 'Bornova', 'İzmir', '35040'),
        ('onur.kilic@demo.kapidamart.local', 'Onur', 'Kılıç', '+90 532 100 0011', 'Kartal', 'İstanbul', '34870'),
        ('defne.aydin@demo.kapidamart.local', 'Defne', 'Aydın', '+90 532 100 0012', 'Muratpaşa', 'Antalya', '07200'),
        ('baris.gunes@demo.kapidamart.local', 'Barış', 'Güneş', '+90 532 100 0013', 'Odunpazarı', 'Eskişehir', '26010'),
        ('irem.koc@demo.kapidamart.local', 'İrem', 'Koç', '+90 532 100 0014', 'Nilüfer', 'Bursa', '16110'),
        ('tolga.ersoy@demo.kapidamart.local', 'Tolga', 'Ersoy', '+90 532 100 0015', 'Sarıyer', 'İstanbul', '34450'),
        ('naz.ozkan@demo.kapidamart.local', 'Naz', 'Özkan', '+90 532 100 0016', 'Çayyolu', 'Ankara', '06810'),
        ('arda.tunc@demo.kapidamart.local', 'Arda', 'Tunç', '+90 532 100 0017', 'Karşıyaka', 'İzmir', '35590'),
        ('sude.akar@demo.kapidamart.local', 'Sude', 'Akar', '+90 532 100 0018', 'Gebze', 'Kocaeli', '41400'),
        ('yigit.bulut@demo.kapidamart.local', 'Yiğit', 'Bulut', '+90 532 100 0019', 'Pendik', 'İstanbul', '34890'),
        ('ceren.dogan@demo.kapidamart.local', 'Ceren', 'Doğan', '+90 532 100 0020', 'Mamak', 'Ankara', '06350'),
        ('kaan.yilmaz@demo.kapidamart.local', 'Kaan', 'Yılmaz', '+90 532 100 0021', 'Buca', 'İzmir', '35390'),
        ('asli.erdem@demo.kapidamart.local', 'Aslı', 'Erdem', '+90 532 100 0022', 'Nilüfer', 'Bursa', '16120'),
        ('furkan.tekin@demo.kapidamart.local', 'Furkan', 'Tekin', '+90 532 100 0023', 'Bağcılar', 'İstanbul', '34200'),
        ('pelin.cetin@demo.kapidamart.local', 'Pelin', 'Çetin', '+90 532 100 0024', 'Çankaya', 'Ankara', '06690'),
        ('umut.korkmaz@demo.kapidamart.local', 'Umut', 'Korkmaz', '+90 532 100 0025', 'Konak', 'İzmir', '35260')
) AS v(email, fn, ln, phone, ad, ci, pc)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.email = v.email);

-- Ürün başına 0–6 ek yorum; farklı kullanıcılar, tekil (user_id, product_id)
INSERT INTO reviews (rating, title, comment, is_verified, is_helpful, user_id, product_id, is_active, created_at, updated_at)
SELECT
    sub.rating,
    sub.title,
    sub.comment,
    sub.is_verified,
    sub.is_helpful,
    sub.user_id,
    sub.product_id,
    TRUE,
    CURRENT_TIMESTAMP - (sub.rn * INTERVAL '2 hours') - (sub.product_id * INTERVAL '17 minutes'),
    CURRENT_TIMESTAMP
FROM (
    SELECT
        p.id AS product_id,
        u.id AS user_id,
        row_number() OVER (PARTITION BY p.id ORDER BY u.id, (p.id * 31 + u.id * 7)) AS rn,
        (3 + (abs(hashtext(concat(p.id::text, u.id::text))) % 3))::int AS rating,
        (ARRAY[
            'Gayet memnun kaldım',
            'Fiyat performans iyi',
            'Kargo hızlı geldi',
            'Kalite beklentimin üstünde',
            'Orta segment için doğru tercih',
            'Paketleme özenliydi',
            'Tekrar alırım',
            'Ailecek beğendik',
            'Ürün orijinal görünüyor',
            'Kurulumu kolay',
            'Biraz gecikti ama sorun yok',
            'Fotoğrafla uyumlu',
            'Tavsiye ederim',
            'İndirimden yakaladım',
            'Günlük kullanıma uygun'
        ])[(abs(hashtext(concat(p.id::text, u.id::text))) % 15) + 1] AS title,
        (ARRAY[
            'Ürün açıklamasıyla birebir uyumlu. Satıcıya teşekkürler.',
            'Hızlı kargo, hasarsız teslim. Performansından memnunum.',
            'Fiyatına göre kalitesi gayet iyi. Arkadaşlarıma da önerdim.',
            'Uzun süredir kullanıyorum, dayanıklı ve kullanışlı.',
            'Beklentimin bir tık altında kaldı ama idare eder.',
            'Kampanyalı fiyattan aldım, pişman değilim.',
            'Müşteri hizmetleri soruma hızlı döndü, teşekkürler.',
            'Küçük bir kozmetik çizik vardı, iade etmedim yine de.',
            'Çocuklar için aldım, çok sevindiler.',
            'Ofiste kullanıyorum, sessiz ve stabil çalışıyor.',
            'Türkiye şartlarında fiyat artmış olsa da değer.',
            'İlk günden itibaren memnunum, yorum yapanlara teşekkür.',
            'Kıyasladım, burada en uygun fiyatı buldum.',
            'Garanti belgesi kutuda çıktı, resmi satış gibi.',
            'Tek eksisi kullanım kılavuzunun kısa olması, internetten tamamladım.'
        ])[(abs(hashtext(concat(u.id::text, p.id::text, 'c'))) % 15) + 1] AS comment,
        (abs(hashtext(concat(p.id::text, u.id::text, 'v'))) % 3) <> 0 AS is_verified,
        (abs(hashtext(concat(p.id::text, u.id::text, 'h'))) % 4) = 0 AS is_helpful
    FROM products p
    INNER JOIN users u ON u.is_active = TRUE AND u.id >= 2
    WHERE p.is_active = TRUE
      AND mod(abs(hashtext(concat(p.id::text, u.id::text))), 17) < 5
) AS sub
WHERE sub.rn <= 6
  AND NOT EXISTS (
      SELECT 1 FROM reviews r
      WHERE r.user_id = sub.user_id AND r.product_id = sub.product_id AND r.is_active = TRUE
  );

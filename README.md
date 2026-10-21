# E-Ticaret API (Spring Boot)

Türkçe bir pazaryeri vitrini için yazılmış REST API. Ürün kataloğu, sepet, sipariş, yorum, bildirim ve kullanıcı ayarlarını yönetiyor. Ön yüz ayrı bir depoda duruyor ve bu servise HTTP üzerinden bağlanıyor.

Projenin tanıtımı, kurulum adımları ve yapısı: **https://mihrimatrix.github.io/backend-spring/**

Aynı sözleşmeyi uygulayan bir .NET sürümü de var. İki backend'in cevapları alan alan aynı; kurallar [docs/API_CONTRACT.md](docs/API_CONTRACT.md) dosyasında.

## Hızlı başlangıç

JDK 17 yeterli. Maven'ı depodaki sarmalayıcı indirir.

```bash
git clone https://github.com/MihrimatriX/backend-spring.git
cd backend-spring
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

Geliştirme profili bellek içi H2 ile açılır ve örnek veriyi kendisi yükler. API `http://127.0.0.1:8081` adresinde dinler.

| Adres | Ne var |
|---|---|
| `/swagger-ui.html` | Swagger arayüzü |
| `/h2-console` | H2 konsolu (JDBC URL `jdbc:h2:mem:testdb`, kullanıcı `sa`, şifre boş) |
| `/actuator/health` | Sağlık durumu |

Hazır hesaplar: `admin@example.com` / `admin123` (yönetici), `user1@example.com` / `user123` (müşteri).

PostgreSQL, Redis ve RabbitMQ ile tam yığın için kök dizinde `docker compose up --build -d` çalıştırın. Host portlarını değiştirmek isterseniz `.env.example` dosyasını `.env` olarak kopyalayın.

Tarayıcıdan deneme ve sık karşılaşılan sorunlar için [TARAYICI.md](TARAYICI.md) dosyasına bakın.

## Yapı

```
src/main/java/com/ecommerce/backend/
├── domain/entity/      JPA entity'leri
├── application/        servisler, DTO'lar, paylaşılan hesaplar, ApiException
└── infrastructure/     controller'lar, güvenlik, repository'ler, ayarlar, mesajlaşma, örnek veri
src/main/resources/db/migration/   Flyway göçleri
docs/                   GitHub Pages sitesi, API sözleşmesi, OpenAPI anlık görüntüsü
scripts/                derleme, dağıtım ve sözleşme duman testi betikleri
```

Servisler kural ihlallerinde `ApiException` fırlatır. `GlobalExceptionHandler` bunu doğru HTTP koduna ve ortak cevap zarfına (`success`, `message`, `data`, `errorCode`) çevirir.

Kullanılanlar: Java 17, Spring Boot 3.4 (Web, Security, Data JPA, Validation, Actuator, Cache), JJWT, springdoc-openapi, Resilience4j, Flyway, PostgreSQL, H2. Redis ve RabbitMQ isteğe bağlı.

## Ayarlar

Profil `SPRING_PROFILES_ACTIVE` ile seçilir: `dev` (varsayılan), `docker` veya `prod`.

| Değişken | Ne için | Varsayılan |
|---|---|---|
| `JWT_SECRET` | Token imzalama anahtarı; üretimde değiştirin | geliştirme anahtarı |
| `ALLOWED_ORIGINS` | CORS'a izin verilen ön yüz adresleri | `localhost:3000`, `:5173`, `:8080` |
| `ADMIN_EMAILS` | Yönetici rolü alacak e-postalar | `admin@example.com,manager@shop.demo` |
| `RATE_LIMIT` | IP başına dakikalık istek sınırı (0 kapatır) | `300` |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL bağlantısı (`prod`) | |

## Test

```bash
./mvnw test
```

Birim, controller ve sipariş akışı entegrasyon testlerini çalıştırır. Uygulama açıkken sözleşme uyumunu da kontrol edebilirsiniz:

```bash
node scripts/contract-smoke.mjs run http://127.0.0.1:8081 out-spring.json
```

## GitHub Pages sitesi

Site `docs/` klasöründen yayımlanır (Settings, Pages, "Deploy from a branch", `main` dalı ve `/docs` klasörü). API uçları değiştiğinde OpenAPI kopyasını uygulama açıkken yenileyin:

```bash
curl -s http://127.0.0.1:8081/v3/api-docs -o docs/openapi.json
```

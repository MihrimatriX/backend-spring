# 🛒 E-Ticaret Backend API

> **Spring Boot 3.3.4** tabanlı, üretim ortamına hazır, kapsamlı özelliklere sahip modern e-ticaret backend uygulaması.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📝 Proje Hakkında

Bu proje, **Spring Boot** ile geliştirilmiş bir backend uygulamasıdır: REST API, güvenlik, veri erişimi, önbellek ve gözlemlenebilirlik Spring ekosistemindeki bileşenlerle (Spring Web, Spring Security, Spring Data JPA, Spring Cache, Actuator vb.) kurulmuştur. Modern e-ticaret platformları için tasarlanmış, ölçeklenebilir ve güvenli bir API sunar; JWT tabanlı kimlik doğrulama, Redis önbellekleme, PostgreSQL veritabanı, Prometheus ve Grafana ile izleme ve Docker desteği ile donatılmıştır.

**Not:** Bu depo bir monorepo değildir; web istemcisi ayrı bir depoda tutulur ve bu API’ye HTTP üzerinden bağlanır. Yerel geliştirmede istemci origin’inizi `ALLOWED_ORIGINS` ile tanımlayın (varsayılanda yaygın localhost portları dahildir). Tarayıcıdan hızlı deneme için bkz. [TARAYICI.md](TARAYICI.md).

## ✨ Özellikler

### 🔐 Kimlik Doğrulama ve Güvenlik
- **JWT Token Tabanlı Kimlik Doğrulama** - Güvenli token tabanlı kimlik doğrulama sistemi
- **Rate Limiting** - Bucket4j ile API istek sınırlandırması
- **CORS Yapılandırması** - Yapılandırılabilir çapraz kaynak paylaşımı
- **Güvenlik Başlıkları** - Kapsamlı güvenlik başlıkları (X-Content-Type-Options, X-Frame-Options, vb.)
- **BCrypt Şifre Şifreleme** - Güvenli şifre hash'leme

### 🗄️ Veritabanı ve Önbellekleme
- **PostgreSQL** - Üretim ortamı için güçlü ilişkisel veritabanı
- **H2 Database** - Geliştirme ortamı için hafıza içi veritabanı
- **Flyway Migration** - Veritabanı versiyonlama ve göç yönetimi
- **Redis Önbellekleme** - Yüksek performans için Redis tabanlı önbellekleme
- **Connection Pooling** - HikariCP ile optimize edilmiş veritabanı bağlantıları

### 📊 İzleme ve Gözlemlenebilirlik
- **Spring Actuator** - Sağlık kontrolleri ve metrikler
- **Prometheus Entegrasyonu** - Özel metrik toplama
- **Grafana Dashboard'ları** - Gerçek zamanlı izleme panelleri
- **Request Tracing** - Benzersiz istek ID takibi
- **Metrik Endpoint'leri** - HTTP istek metrikleri, JVM metrikleri, özel e-ticaret metrikleri

### 🚀 API Özellikleri
- **RESTful API** - REST mimarisi prensiplerine uygun API tasarımı
- **Swagger/OpenAPI Dokümantasyonu** - Interaktif API dokümantasyonu
- **Validation** - Bean Validation ile giriş doğrulama
- **Exception Handling** - Merkezi hata yönetimi
- **Async Processing** - Arka plan görev işleme

### 🐳 Containerization ve Deployment
- **Docker Desteği** - Çok aşamalı Docker build'leri
- **Docker Compose** - Geliştirme ve üretim ortamları için hazır yapılandırmalar
- **Health Checks** - Kapsamlı sağlık izleme
- **Multi-Environment Support** - Geliştirme ve üretim yapılandırmaları

## 🏗️ Mimari

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Load Balancer │    │   API Gateway   │
│   (React/Vue)   │◄──►│   (Nginx)       │◄──►│   (Spring)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                       ┌─────────────────────────────────┼─────────────────────────────────┐
                       │                                 │                                 │
                       ▼                                 ▼                                 ▼
              ┌─────────────────┐              ┌─────────────────┐              ┌─────────────────┐
              │   PostgreSQL    │              │     Redis       │              │   Monitoring    │
              │   Database      │              │     Cache       │              │  (Prometheus)   │
              └─────────────────┘              └─────────────────┘              └─────────────────┘
```

## 🛠️ Teknoloji Stack'i

| Teknoloji | Versiyon | Açıklama |
|-----------|----------|----------|
| **Spring Boot** | 3.3.4 | Ana framework |
| **Java** | 17 | Programlama dili |
| **PostgreSQL** | 15 | Üretim veritabanı |
| **H2 Database** | - | Geliştirme veritabanı |
| **Redis** | 7 | Önbellekleme sistemi |
| **JWT (JJWT)** | 0.12.3 | Token tabanlı kimlik doğrulama |
| **Flyway** | 10.10.0 | Veritabanı migration |
| **Prometheus** | - | Metrik toplama |
| **Grafana** | - | İzleme dashboard'ları |
| **Docker** | - | Containerization |
| **Maven** | - | Build tool |
| **MapStruct** | 1.6.3 | Object mapping |
| **Bucket4j** | 8.0.1 | Rate limiting |
| **SpringDoc OpenAPI** | 2.6.0 | API dokümantasyonu |

## 📋 Gereksinimler

- **Java** 17 veya üzeri
- **Maven** 3.6+ veya üzeri
- **Docker** & **Docker Compose** (opsiyonel)
- **Git**

## 🚀 Hızlı Başlangıç

### 1. Repository'yi Klonlayın

```bash
git clone <repository-url>
cd backend-spring
```

### 2. Geliştirme Ortamı

#### Docker ile Çalıştırma

```bash
# Geliştirme ortamını başlat
docker-compose -f docker/docker-compose.dev.yml up -d

# Logları görüntüle
docker-compose -f docker/docker-compose.dev.yml logs -f
```

#### Lokal Çalıştırma

```bash
# Maven ile çalıştır
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Veya build edip çalıştır
mvn clean package -DskipTests
java -jar target/ecommerce-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

İsteğe bağlı (Node yüklüyse): `npm install && npm run dev` kök dizinde yalnızca Spring Boot’u `mvnw` ile başlatır.

### 3. Üretim Ortamı

```bash
# Build ve deploy
./scripts/build.sh --profile prod
./scripts/deploy.sh --env prod
```

## 🔧 Yapılandırma

### Ortam Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|------------|
| `SPRING_PROFILES_ACTIVE` | Aktif profil | `dev` |
| `DB_HOST` | Veritabanı host'u | `localhost` |
| `DB_PORT` | Veritabanı portu | `5432` |
| `DB_NAME` | Veritabanı adı | `ecommerce_db` |
| `DB_USERNAME` | Veritabanı kullanıcı adı | `ecommerce_user` |
| `DB_PASSWORD` | Veritabanı şifresi | `ecommerce_password` |
| `REDIS_HOST` | Redis host'u | `localhost` |
| `REDIS_PORT` | Redis portu | `6379` |
| `JWT_SECRET` | JWT secret key | `mySecretKeyThatIsAtLeast256BitsLongForJWTTokenSecurity` |
| `JWT_EXPIRATION` | JWT expiration (ms) | `86400000` (24 saat) |
| `ALLOWED_ORIGINS` | CORS izin verilen origin'ler | `http://localhost:3000,http://localhost:8080` |
| `RATE_LIMIT` | Dakikada istek limiti | `100` |

### Uygulama Profilleri

#### Development (`application-dev.yml`)
- H2 in-memory veritabanı
- Debug logging aktif
- H2 console aktif
- Flyway devre dışı
- Detaylı hata mesajları

#### Production (`application-prod.yml`)
- PostgreSQL veritabanı
- Optimize edilmiş JPA ayarları
- Flyway aktif
- Redis önbellekleme
- Üretim logging seviyesi
- Güvenlik optimizasyonları

#### Docker (`application-docker.yml`)
- Docker ortamı için özel yapılandırma
- Container içi servis bağlantıları

## 📊 API Endpoints

### Kimlik Doğrulama
- `POST /api/auth/register` - Kullanıcı kaydı
- `POST /api/auth/login` - Kullanıcı girişi
- `POST /api/auth/logout` - Kullanıcı çıkışı

### Ürünler
- `GET /api/product` - Tüm ürünleri getir (sayfalama ile)
- `GET /api/product/{id}` - ID'ye göre ürün getir
- `POST /api/product` - Ürün oluştur (Admin)
- `PUT /api/product/{id}` - Ürün güncelle (Admin)
- `DELETE /api/product/{id}` - Ürün sil (Admin)

### Kategoriler
- `GET /api/category` - Tüm kategorileri getir
- `GET /api/category/{id}` - ID'ye göre kategori getir
- `POST /api/category` - Kategori oluştur (Admin)
- `PUT /api/category/{id}` - Kategori güncelle (Admin)
- `DELETE /api/category/{id}` - Kategori sil (Admin)

### Siparişler
- `GET /api/order` - Kullanıcı siparişlerini getir
- `GET /api/order/{id}` - ID'ye göre sipariş getir
- `POST /api/order` - Sipariş oluştur
- `PUT /api/order/{id}/status` - Sipariş durumu güncelle

### Sepet
- `GET /api/cart` - Sepet içeriğini getir
- `POST /api/cart/add` - Sepete ürün ekle
- `PUT /api/cart/update` - Sepet öğesi güncelle
- `DELETE /api/cart/remove/{itemId}` - Sepetten ürün çıkar

### Favoriler
- `GET /api/favorite` - Favori ürünleri getir
- `POST /api/favorite` - Favorilere ekle
- `DELETE /api/favorite/{id}` - Favorilerden çıkar

### İncelemeler
- `GET /api/review/product/{productId}` - Ürün incelemelerini getir
- `POST /api/review` - İnceleme oluştur
- `PUT /api/review/{id}` - İnceleme güncelle
- `DELETE /api/review/{id}` - İnceleme sil

### Sağlık ve İzleme
- `GET /actuator/health` - Uygulama sağlık durumu
- `GET /actuator/prometheus` - Prometheus metrikleri
- `GET /swagger-ui.html` - API dokümantasyonu
- `GET /v3/api-docs` - OpenAPI JSON

## 🔍 İzleme

### Prometheus Metrikleri
- HTTP istek metrikleri
- JVM metrikleri
- Özel e-ticaret metrikleri
- Veritabanı bağlantı metrikleri

### Grafana Dashboard'ları
- Uygulama performansı
- İş metrikleri
- Altyapı izleme
- Hata takibi

### Erişim URL'leri

| Servis | URL | Kimlik Bilgileri |
|--------|-----|------------------|
| **Uygulama** | http://localhost:8081 | - |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin/admin123 |
| **H2 Console (Dev)** | http://localhost:8082 | - |

## 🐳 Docker

### Geliştirme Ortamı

```bash
docker-compose -f docker/docker-compose.dev.yml up -d
```

### Üretim Ortamı

```bash
docker-compose -f docker/docker-compose.yml up -d
```

### Servisler
- **app**: Spring Boot uygulaması
- **postgres**: PostgreSQL veritabanı
- **redis**: Redis önbellek
- **prometheus**: Metrik toplama
- **grafana**: İzleme dashboard'ları

### Docker Build

```bash
# Docker image oluştur
docker build -t ecommerce-backend:latest .

# Docker Compose ile çalıştır
docker-compose up -d
```

## 🧪 Test

### Unit Testler

```bash
mvn test
```

### Integration Testler

```bash
mvn verify
```

### Health Check

```bash
./scripts/health-check.sh
```

## 📈 Performans Optimizasyonu

### Veritabanı
- HikariCP ile connection pooling
- Optimize edilmiş JPA ayarları
- Veritabanı indexleri
- Sorgu optimizasyonu

### Önbellekleme
- Redis tabanlı önbellekleme
- Cache eviction stratejileri
- TTL yapılandırması

### Async İşleme
- Arka plan görev işleme
- Thread pool yapılandırması
- E-posta bildirimleri
- Sistem bildirimleri

## 🔒 Güvenlik

### Kimlik Doğrulama
- JWT token tabanlı kimlik doğrulama
- BCrypt ile şifre şifreleme
- Token expiration handling

### Yetkilendirme
- Rol tabanlı erişim kontrolü
- Endpoint güvenliği
- Metod seviyesi güvenlik

### Güvenlik Başlıkları
- X-Content-Type-Options
- X-Frame-Options
- X-XSS-Protection
- Strict-Transport-Security
- Content-Security-Policy

### Rate Limiting
- API rate limiting
- IP tabanlı sınırlandırma
- Yapılandırılabilir limitler

## 🚀 Deployment

### Build Script

```bash
./scripts/build.sh --profile prod --tag v1.0.0
```

### Deploy Script

```bash
./scripts/deploy.sh --env prod --backup
```

### Health Check

```bash
./scripts/health-check.sh --url http://your-domain.com
```

**Tags**: `spring-boot` `java` `e-commerce` `backend` `api` `rest` `jwt` `postgresql` `redis` `docker` `microservices` `prometheus` `grafana` `maven` `flyway` `swagger` `openapi`

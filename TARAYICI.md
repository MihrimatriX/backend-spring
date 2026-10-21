# Tarayıcıdan deneme (yerel, yalnızca API)

Bu depo **yalnızca backend** içerir. Ön yüz ayrı bir depodaysa orada API taban adresini (ör. `http://127.0.0.1:8081`) ve gerekirse Vite/proxy ayarlarını o repodaki dokümana göre yapın; burada CORS için `ALLOWED_ORIGINS` içinde istemci origin’inizin (ör. `http://localhost:5173`) bulunduğundan emin olun.

## Ne açılır?

- **API:** varsayılan **http://127.0.0.1:8081**
- **Swagger:** http://127.0.0.1:8081/swagger-ui.html (uygulama ayağa kalkınca)
- **Sağlık:** http://127.0.0.1:8081/actuator/health

## Maven ile (önerilen)

```powershell
cd <bu-depo-kökü>
.\mvnw.cmd spring-boot:run
```

`Started EcommerceBackendApplication` satırını bekleyin; ardından Swagger’ı tarayıcıda açın.

## İsteğe bağlı: kökten `npm run dev`

Node yüklüyse aynı işi script ile de başlatabilirsiniz:

```powershell
cd <bu-depo-kökü>
npm install
npm run dev
```

Bu yalnızca Spring Boot’u çalıştırır (`scripts/start-backend.cjs` → `mvnw spring-boot:run`).

## Docker ile API

Proje kökünde `docker compose up --build -d` (veya `stack.cmd`) sonrası API genelde **http://127.0.0.1:8081**. Aynı makinede Maven ile ikinci kez `spring-boot:run` veya `npm run dev` çalıştırmayın (port çakışır).

## Demo hesap (yalnızca `dev` profilinde DataSeeder)

| E-posta             | Şifre     |
|---------------------|-----------|
| `user1@example.com` | `user123` |
| `admin@example.com` | `admin123` |

## Kontrol listesi

- H2 konsol (dev): http://127.0.0.1:8081/h2-console (JDBC URL: `jdbc:h2:mem:testdb`, kullanıcı `sa`, şifre boş)

## Sorun giderme

- **`ECONNREFUSED` / boş Swagger:** API henüz dinlemiyor veya port farklı; `docker compose ps` veya konsolda Spring logunu kontrol edin.
- **CORS:** Ayrı depodaki ön yüz farklı origin’den istek atıyorsa `ALLOWED_ORIGINS` veya ilgili profil YAML’ına o origin’i ekleyin.
- **JAVA_HOME:** JDK 17 kurulu dizini `JAVA_HOME` olarak ayarlayın.
- **Port meşgul (`8081`):** Çakışan süreci kapatın veya `application-dev.yml` içinde sunucu portunu değiştirin.

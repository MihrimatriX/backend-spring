# Yerel API: Spring Boot (8081). Swagger icin tarayici kullanin.
# Alternatif: .\mvnw.cmd spring-boot:run veya npm run dev
$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "HATA: java bulunamadi. JDK 17 kurun veya PATH'e ekleyin." -ForegroundColor Red
    exit 1
}

Write-Host "Backend aciliyor: http://127.0.0.1:8081" -ForegroundColor Cyan
Start-Process powershell -WorkingDirectory $root -ArgumentList @(
    "-NoExit", "-Command", ".\mvnw.cmd spring-boot:run"
)

Write-Host ""
Write-Host "API ayakta olunca:" -ForegroundColor Green
Write-Host "  Swagger  http://127.0.0.1:8081/swagger-ui.html" -ForegroundColor Green
Write-Host "  Saglik   http://127.0.0.1:8081/actuator/health" -ForegroundColor Green
Write-Host "On yuz bu depoda degil; ayri repoda API taban URL'ini bu porta yonlendirin." -ForegroundColor DarkGray
Write-Host "Detay: TARAYICI.md" -ForegroundColor Gray

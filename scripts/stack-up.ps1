# Tüm yığın: Postgres + Redis + RabbitMQ + Flyway (migrate + SQL seed) + API
# Önce proje kökünde olun veya:  .\scripts\stack-up.ps1
$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)

docker compose up --build -d

Write-Host ""
Write-Host "Tamam. Portlar için proje kökündeki .env.example -> .env" -ForegroundColor DarkGray
Write-Host "Varsayilan: API http://localhost:8081/actuator/health | Swagger .../swagger-ui.html" -ForegroundColor Green

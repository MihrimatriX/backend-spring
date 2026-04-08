@echo off
cd /d "%~dp0"
docker compose up --build -d
echo.
echo Varsayilan: http://localhost:8081/actuator/health

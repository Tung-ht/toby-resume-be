@echo off
REM Development deploy: MongoDB + App (no Jenkins). Run from repo root or any folder.
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%..\.."

if not exist ".env" (
  copy ".env.example" ".env"
  echo Created .env from .env.example - edit with your values.
)

echo Starting MongoDB + App (dev mode)...
docker compose -f deploy/docker-compose.dev.yml up -d --build
if errorlevel 1 (
  echo docker compose failed
  exit /b 1
)
echo.
echo Dev stack started.
echo   App:     http://localhost:8080
echo   Health:  http://localhost:8080/actuator/health
echo   GraphQL: http://localhost:8080/graphql
echo   Swagger: http://localhost:8080/swagger-ui.html
echo   MongoDB: localhost:27017

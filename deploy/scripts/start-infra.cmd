@echo off
REM Start infrastructure (MongoDB + Jenkins). Run from repo root or any folder.
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%..\.."

echo Starting infrastructure (MongoDB + Jenkins)...
docker compose -f deploy/docker-compose.yml up -d --build
if errorlevel 1 (
  echo docker compose failed
  exit /b 1
)
echo.
echo Infrastructure started.
echo   MongoDB:  localhost:27017
echo   Jenkins:  http://localhost:8081
echo.
echo Next: open Jenkins and click 'Build Now' to deploy the backend app.

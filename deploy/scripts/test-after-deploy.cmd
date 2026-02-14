@echo off
setlocal enabledelayedexpansion
REM Smoke tests after deploy. Optional: pass BaseUrl (default http://localhost:8080)
REM Usage: test-after-deploy.cmd [BaseUrl]
set "BASE_URL=%~1"
if "%BASE_URL%"=="" set "BASE_URL=http://localhost:8080"
set "FAILED=0"

echo.
echo --- Health ---
curl -sf -o NUL "%BASE_URL%/actuator/health"
if errorlevel 1 (echo [FAIL] Actuator health & set FAILED=1) else echo [OK] Actuator health - 200

echo.
echo --- Public API ---
curl -sf -X POST -H "Content-Type: application/json" -d "{""query"":""query { siteSettings { siteName } }""}" "%BASE_URL%/graphql" -o NUL
if errorlevel 1 (echo [FAIL] GraphQL siteSettings & set FAILED=1) else echo [OK] GraphQL siteSettings - 200
curl -sf -o NUL "%BASE_URL%/swagger-ui.html"
if errorlevel 1 (echo [FAIL] Swagger UI & set FAILED=1) else echo [OK] Swagger UI - 200

echo.
echo --- Summary ---
if !FAILED!==0 (echo All checks passed. & exit /b 0) else (echo One or more checks failed. & exit /b 1)

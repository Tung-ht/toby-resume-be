@echo off
REM Stop all containers (infrastructure + app).
REM Usage: down.cmd [dev]  — pass "dev" to stop dev stack instead
set "SCRIPT_DIR=%~dp0"
cd /d "%SCRIPT_DIR%..\.."

if /i "%1"=="dev" goto devstack
if /i "%1"=="-dev" goto devstack
if /i "%1"=="--dev" goto devstack

echo Stopping infrastructure (MongoDB + Jenkins)...
docker compose -f deploy/docker-compose.yml down

REM Also stop the app container if it was deployed by Jenkins
docker ps -q -f name=tobyresume-app >nul 2>&1 && (
  echo Stopping app container (deployed by Jenkins)...
  docker stop tobyresume-app
  docker rm tobyresume-app
)
goto done

:devstack
echo Stopping dev stack (app + MongoDB)...
docker compose -f deploy/docker-compose.dev.yml down

:done
echo Stack stopped.

# Development deploy: MongoDB + App (no Jenkins). Run from repo root.
# Usage: .\deploy\scripts\deploy-dev.ps1 [-RepoRoot "D:\TobyResume\BE"]
param([string]$RepoRoot = $PSScriptRoot + "\..\..")
Set-Location $RepoRoot

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example — edit with your values." -ForegroundColor Yellow
}

Write-Host "Starting MongoDB + App (dev mode)..."
docker compose -f deploy/docker-compose.dev.yml up -d --build
if ($LASTEXITCODE -ne 0) { Write-Error "docker compose failed"; exit $LASTEXITCODE }

Write-Host ""
Write-Host "Dev stack started." -ForegroundColor Green
Write-Host "  App:     http://localhost:8080"
Write-Host "  Health:  http://localhost:8080/actuator/health"
Write-Host "  GraphQL: http://localhost:8080/graphql"
Write-Host "  Swagger: http://localhost:8080/swagger-ui.html"
Write-Host "  MongoDB: localhost:27017"

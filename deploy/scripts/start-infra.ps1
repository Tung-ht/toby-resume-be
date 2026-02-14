# Start infrastructure (MongoDB + Jenkins). Run from repo root.
# Usage: .\deploy\scripts\start-infra.ps1 [-RepoRoot "D:\TobyResume\BE"]
param([string]$RepoRoot = $PSScriptRoot + "\..\..")
Set-Location $RepoRoot

Write-Host "Starting infrastructure (MongoDB + Jenkins)..."
docker compose -f deploy/docker-compose.yml up -d --build
if ($LASTEXITCODE -ne 0) { Write-Error "docker compose failed"; exit $LASTEXITCODE }

Write-Host ""
Write-Host "Infrastructure started." -ForegroundColor Green
Write-Host "  MongoDB:  localhost:27017"
Write-Host "  Jenkins:  http://localhost:8081"
Write-Host ""
Write-Host "Next: open Jenkins and click 'Build Now' to deploy the backend app."

# Stop all containers (infrastructure + app). Run from repo root.
# Usage: .\deploy\scripts\down.ps1 [-Dev]
#   -Dev  Stop dev stack (docker-compose.dev.yml) instead of infrastructure
param([switch]$Dev, [string]$RepoRoot = $PSScriptRoot + "\..\..")
Set-Location $RepoRoot

if ($Dev) {
    Write-Host "Stopping dev stack (app + MongoDB)..."
    docker compose -f deploy/docker-compose.dev.yml down
} else {
    Write-Host "Stopping infrastructure (MongoDB + Jenkins)..."
    docker compose -f deploy/docker-compose.yml down

    # Also stop the app container if it was deployed by Jenkins
    $appRunning = docker ps -q -f name=tobyresume-app 2>$null
    if ($appRunning) {
        Write-Host "Stopping app container (deployed by Jenkins)..."
        docker stop tobyresume-app
        docker rm tobyresume-app
    }
}

Write-Host "Stack stopped." -ForegroundColor Green

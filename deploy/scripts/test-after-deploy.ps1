# Smoke tests after deploy. Run from repo root. Base URL default: http://localhost:8080
# Usage: .\deploy\scripts\test-after-deploy.ps1 [-BaseUrl "http://localhost:8080"]
param([string]$BaseUrl = "http://localhost:8080")
$ErrorActionPreference = "Stop"
$failed = 0

function Test-Endpoint {
    param([string]$Name, [string]$Url, [string]$Method = "GET", [string]$Body = $null)
    try {
        $params = @{ Uri = $Url; Method = $Method; UseBasicParsing = $true; TimeoutSec = 10 }
        if ($Body) { $params.Body = $Body; $params.ContentType = "application/json" }
        $r = Invoke-WebRequest @params
        Write-Host "[OK] $Name -> $($r.StatusCode)" -ForegroundColor Green
        return $true
    } catch {
        Write-Host "[FAIL] $Name -> $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

Write-Host "`n--- Health ---"
if (-not (Test-Endpoint "Actuator health" "$BaseUrl/actuator/health")) { $failed++ }

Write-Host "`n--- Public API ---"
$graphqlBody = '{"query":"query { siteSettings { siteName } }"}'
if (-not (Test-Endpoint "GraphQL siteSettings" "$BaseUrl/graphql" "POST" $graphqlBody)) { $failed++ }
if (-not (Test-Endpoint "Swagger UI" "$BaseUrl/swagger-ui.html")) { $failed++ }

Write-Host "`n--- Summary ---"
if ($failed -eq 0) { Write-Host "All checks passed." -ForegroundColor Green; exit 0 }
else { Write-Host "$failed check(s) failed." -ForegroundColor Red; exit 1 }

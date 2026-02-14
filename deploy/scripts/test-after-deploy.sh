#!/usr/bin/env bash
# Smoke tests after deploy. Optional: pass BaseUrl (default http://localhost:8080)
# Usage: ./test-after-deploy.sh [BaseUrl]
BASE_URL="${1:-http://localhost:8080}"
FAILED=0

echo ""
echo "--- Health ---"
if curl -sf --max-time 10 "$BASE_URL/actuator/health" > /dev/null; then
  echo "[OK] Actuator health -> 200"
else
  echo "[FAIL] Actuator health"
  FAILED=$((FAILED + 1))
fi

echo ""
echo "--- Public API ---"
if curl -sf --max-time 10 -X POST -H "Content-Type: application/json" \
  -d '{"query":"query { siteSettings { siteName } }"}' "$BASE_URL/graphql" > /dev/null; then
  echo "[OK] GraphQL siteSettings -> 200"
else
  echo "[FAIL] GraphQL siteSettings"
  FAILED=$((FAILED + 1))
fi
if curl -sf --max-time 10 "$BASE_URL/swagger-ui.html" > /dev/null; then
  echo "[OK] Swagger UI -> 200"
else
  echo "[FAIL] Swagger UI"
  FAILED=$((FAILED + 1))
fi

echo ""
echo "--- Summary ---"
if [ "$FAILED" -eq 0 ]; then
  echo "All checks passed."
  exit 0
else
  echo "$FAILED check(s) failed."
  exit 1
fi

#!/usr/bin/env bash
# Start infrastructure (MongoDB + Jenkins). Run from repo root or any folder.
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

echo "Starting infrastructure (MongoDB + Jenkins)..."
docker compose -f deploy/docker-compose.yml up -d --build

echo ""
echo "Infrastructure started."
echo "  MongoDB:  localhost:27017"
echo "  Jenkins:  http://localhost:8081"
echo ""
echo "Next: open Jenkins and click 'Build Now' to deploy the backend app."

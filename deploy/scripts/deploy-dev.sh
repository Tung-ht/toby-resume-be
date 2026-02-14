#!/usr/bin/env bash
# Development deploy: MongoDB + App (no Jenkins). Run from repo root or any folder.
set -e
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "Created .env from .env.example — edit with your values."
fi

echo "Starting MongoDB + App (dev mode)..."
docker compose -f deploy/docker-compose.dev.yml up -d --build

echo ""
echo "Dev stack started."
echo "  App:     http://localhost:8080"
echo "  Health:  http://localhost:8080/actuator/health"
echo "  GraphQL: http://localhost:8080/graphql"
echo "  Swagger: http://localhost:8080/swagger-ui.html"
echo "  MongoDB: localhost:27017"

#!/usr/bin/env bash
# Stop all containers (infrastructure + app).
# Usage: ./down.sh [--dev]
#   --dev  Stop dev stack (docker-compose.dev.yml) instead of infrastructure
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

if [ "$1" = "--dev" ] || [ "$1" = "-d" ]; then
  echo "Stopping dev stack (app + MongoDB)..."
  docker compose -f deploy/docker-compose.dev.yml down
else
  echo "Stopping infrastructure (MongoDB + Jenkins)..."
  docker compose -f deploy/docker-compose.yml down

  # Also stop the app container if it was deployed by Jenkins
  if docker ps -q -f name=tobyresume-app | grep -q .; then
    echo "Stopping app container (deployed by Jenkins)..."
    docker stop tobyresume-app && docker rm tobyresume-app
  fi
fi

echo "Stack stopped."

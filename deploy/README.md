# Deployment — Quick Start

Run from **repository root** (e.g. `D:\TobyResume\BE` or `/path/to/BE`).

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  Docker Host                                            │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  MongoDB      │  │  Jenkins      │  │  Backend App │  │
│  │  (always on)  │  │  (always on)  │  │  (deployed   │  │
│  │              │  │              │  │   by Jenkins) │  │
│  │  :27017      │  │  :8081       │  │  :8080       │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│        ↑                   │                ↑           │
│        └───────────────────┼────────────────┘           │
│                    tobyresume-network                    │
└─────────────────────────────────────────────────────────┘
```

- **MongoDB + Jenkins** run as infrastructure (started once via `docker compose`).
- **Backend App** is built and deployed by the Jenkins pipeline (click "Build Now").

## 1. Start infrastructure

| Platform   | Command |
|-----------|---------|
| PowerShell | `.\deploy\scripts\start-infra.ps1` |
| Bash       | `./deploy/scripts/start-infra.sh` |
| CMD        | `deploy\scripts\start-infra.cmd` |

Or manually: `docker compose -f deploy/docker-compose.yml up -d`

This starts **MongoDB** (port 27017) and **Jenkins** (port 8081).

## 2. Configure Jenkins (first time only)

1. Open http://localhost:8081
2. **New Item** → name: `tobyresume-backend` → **Pipeline** → OK
3. **Pipeline** section:
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** your repo URL (e.g. `https://github.com/your-org/BE.git`)
   - **Branch:** `*/main`
   - **Script Path:** `deploy/Jenkinsfile`
4. Save.

## 3. Deploy the app (one-click)

Click **Build Now** in Jenkins. The pipeline will:

1. **Checkout** — Fetch latest code from Git
2. **Stop App** — Stop the running backend container (if any)
3. **DB Migration** — Check for new MongoDB scripts in `deploy/db-migrations/` and execute them
4. **Build** — Build the Docker image (compiles code, runs tests, packages JAR)
5. **Deploy** — Start the new backend container on `tobyresume-network`
6. **Health Check** — Wait for `/actuator/health` to return UP

If any step fails, the pipeline stops and shows the error log.

## 4. Test after deploy

| Platform   | Command |
|-----------|---------|
| PowerShell | `.\deploy\scripts\test-after-deploy.ps1` |
| Bash       | `./deploy/scripts/test-after-deploy.sh` |
| CMD        | `deploy\scripts\test-after-deploy.cmd` |

## 5. Stop everything

| Platform   | Command |
|-----------|---------|
| PowerShell | `.\deploy\scripts\down.ps1` |
| Bash       | `./deploy/scripts/down.sh` |
| CMD        | `deploy\scripts\down.cmd` |

This stops MongoDB, Jenkins, **and** the app container.

## Development mode (no Jenkins)

For local development without Jenkins:

| Platform   | Command |
|-----------|---------|
| PowerShell | `.\deploy\scripts\deploy-dev.ps1` |
| Bash       | `./deploy/scripts/deploy-dev.sh` |
| CMD        | `deploy\scripts\deploy-dev.cmd` |

Or: `docker compose -f deploy/docker-compose.dev.yml up -d --build`

Stop: `.\deploy\scripts\down.ps1 -Dev` (or `./deploy/scripts/down.sh --dev`)

## Scripts summary

| Script | .cmd | .sh | .ps1 | Purpose |
|--------|------|-----|------|---------|
| start-infra | ✓ | ✓ | ✓ | Start MongoDB + Jenkins |
| deploy-dev | ✓ | ✓ | ✓ | Dev: MongoDB + App (no Jenkins) |
| down | ✓ | ✓ | ✓ | Stop all containers |
| test-after-deploy | ✓ | ✓ | ✓ | Smoke tests |

## Files and docs

| What | Where |
|------|--------|
| App Dockerfile | `deploy/Dockerfile` |
| Infrastructure compose | `deploy/docker-compose.yml` (MongoDB + Jenkins) |
| Dev compose | `deploy/docker-compose.dev.yml` (MongoDB + App) |
| Jenkins Dockerfile | `deploy/jenkins/Dockerfile` |
| Jenkins pipeline | `deploy/Jenkinsfile` |
| DB migrations | `deploy/db-migrations/` (see README inside) |
| Env template | Root: `.env.example` → copy to `.env` |
| Full guides | [docs/ai/deployment/](../docs/ai/deployment/README.md) |

## URLs

| Service | URL |
|---------|-----|
| App | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health |
| GraphQL | http://localhost:8080/graphql |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Jenkins | http://localhost:8081 |
| MongoDB | localhost:27017 |

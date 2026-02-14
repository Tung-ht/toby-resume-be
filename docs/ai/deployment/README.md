---
phase: deployment
title: Deployment Strategy
description: Define deployment process, infrastructure, and release procedures
---

# Deployment Strategy

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│  Docker Host                                            │
│                                                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  MongoDB      │  │  Jenkins      │  │  Backend App │  │
│  │  (infra)      │  │  (infra)      │  │  (pipeline)  │  │
│  │  :27017      │  │  :8081       │  │  :8080       │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                    tobyresume-network                    │
└─────────────────────────────────────────────────────────┘
```

- **Infrastructure** (MongoDB + Jenkins): started once, always running via `docker-compose.yml`.
- **Backend App**: built and deployed by the Jenkins pipeline. Not part of docker-compose.

## Quick start (scripts)

From **repo root**:

| Goal | Command |
|------|---------|
| Start infrastructure (MongoDB + Jenkins) | `.\deploy\scripts\start-infra.ps1` |
| Deploy app via Jenkins | Open http://localhost:8081 → **Build Now** |
| Dev mode (MongoDB + App, no Jenkins) | `.\deploy\scripts\deploy-dev.ps1` |
| Test after deploy | `.\deploy\scripts\test-after-deploy.ps1` |
| Stop everything | `.\deploy\scripts\down.ps1` |
| Stop dev stack | `.\deploy\scripts\down.ps1 -Dev` |

See **[deploy/README.md](../../../deploy/README.md)** for one-page reference.

## Deployment file layout

| Item | Location |
|------|----------|
| App Dockerfile (multi-stage) | Root: `Dockerfile` |
| Infrastructure compose | Root: `docker-compose.yml` (MongoDB + Jenkins) |
| Dev compose | Root: `docker-compose.dev.yml` (MongoDB + App) |
| Jenkins custom image | `deploy/jenkins/Dockerfile` (Jenkins LTS + Docker CLI) |
| CI/CD pipeline | Root: `Jenkinsfile` |
| DB migration scripts | `deploy/db-migrations/*.js` |
| Deploy scripts | `deploy/scripts/*.ps1` / `*.sh` / `*.cmd` |
| Env template | Root: `.env.example` → copy to `.env` |

## Local deployment (Docker, Jenkins)

Full step-by-step:

- **[Local deployment guide](local-deployment.md)** — Prerequisites, infrastructure setup, Jenkins configuration, one-click deploy.
- **[Test after deploy](test-after-deploy.md)** — Health, GraphQL, Swagger, optional auth checks.

Raw commands (no scripts):

```bash
# Start infrastructure (MongoDB + Jenkins)
docker compose up -d

# Or: MongoDB only (for local dev without Jenkins)
docker compose up -d mongo

# Dev mode: MongoDB + App (no Jenkins)
docker compose -f docker-compose.dev.yml up -d --build
```

---

## 1. Jenkins Pipeline (One-Click Build & Deploy)

The project includes a **Jenkinsfile** that implements the full CI/CD pipeline.

### 1.1 What the pipeline does

When you click **Build Now**, Jenkins executes these stages in order:

| # | Stage | What happens | On failure |
|---|-------|-------------|------------|
| 1 | **Checkout** | Fetches the latest code from the configured Git branch | Pipeline stops |
| 2 | **Stop App** | Stops and removes the running `tobyresume-app` container (if any) | Pipeline stops |
| 3 | **DB Migration** | Scans `deploy/db-migrations/` for new `.js` scripts; runs them against MongoDB via `mongosh` | Pipeline stops |
| 4 | **Build** | Runs `docker build` (multi-stage: Maven compile+test+package → JRE runtime image) | Pipeline stops |
| 5 | **Deploy** | Starts new `tobyresume-app` container on `tobyresume-network` with env vars | Pipeline stops |
| 6 | **Health Check** | Polls `/actuator/health` for up to 150 seconds | Pipeline stops, shows container logs |

**Fail-fast**: any failed stage stops the entire pipeline and shows the error log.
**Warnings**: non-fatal warnings (e.g., "first deploy" notices) are logged but do not stop the pipeline.

### 1.2 Prerequisites

- **Docker** and **Docker Compose** (v2) installed and running.
- **Infrastructure running**: `docker compose up -d` (MongoDB + Jenkins).
- **Jenkins plugins**: Pipeline, Git (installed by default with suggested plugins).
- **Git repository**: accessible from the Jenkins container (public repo or credentials configured).

### 1.3 Create the pipeline job (first time)

1. Open Jenkins at http://localhost:8081.
2. **New Item** → name: `tobyresume-backend` → **Pipeline** → OK.
3. **Pipeline** section:
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** your repo URL (e.g. `https://github.com/your-org/BE.git` or SSH)
   - **Credentials:** add if the repo is private
   - **Branch:** `*/main` (or your default branch)
   - **Script Path:** `Jenkinsfile`
4. Save.

### 1.4 One-click deploy

Click **Build Now**. Jenkins will run all 6 stages. On success, the app is live at http://localhost:8080.

### 1.5 Database migrations

MongoDB migration scripts go in `deploy/db-migrations/`. See [deploy/db-migrations/README.md](../../../deploy/db-migrations/README.md) for naming convention and usage.

The pipeline:
- Detects new `.js` files (sorted alphabetically).
- Checks the `_schema_migrations` collection for already-applied scripts.
- Runs only new scripts via `docker exec tobyresume-mongo mongosh`.
- Records each successful migration in `_schema_migrations`.
- **Fails immediately** if any script errors.

### 1.6 Environment configuration

On first deploy, the pipeline copies `.env.example` to `$JENKINS_HOME/tobyresume.env`. This file persists across builds (stored in the Jenkins volume).

**After first deploy**: edit the env file with production values:

```bash
docker exec -it tobyresume-jenkins bash
vi /var/jenkins_home/tobyresume.env
```

The `MONGODB_URI` is set automatically by the pipeline (points to `tobyresume-mongo:27017`).

### 1.7 Optional: automatic builds

- **Poll SCM:** In the job, under **Build Triggers** → **Poll SCM**, set a schedule (e.g. `H/5 * * * *`).
- **Webhook:** Use GitHub/GitLab webhook to trigger builds on push.

### 1.8 Build artifacts

Each build creates a Docker image tagged with the build number and `latest`:
- `tobyresume-app:42` (build #42)
- `tobyresume-app:latest` (most recent)

---

## 2. Docker & Compose

### Infrastructure (`docker-compose.yml`)

Defines MongoDB and Jenkins. Always running on the server.

| Service | Container name | Port | Purpose |
|---------|---------------|------|---------|
| mongo | tobyresume-mongo | 27017 | Database |
| jenkins | tobyresume-jenkins | 8081 | CI/CD |

All services share the `tobyresume-network` Docker network.

### Development (`docker-compose.dev.yml`)

For local development without Jenkins. Defines MongoDB + App.

```bash
docker compose -f docker-compose.dev.yml up -d --build
```

### App Dockerfile

Multi-stage build:
- **Stage 1 (build):** Maven image compiles, tests, and packages the JAR.
- **Stage 2 (run):** JRE image with only the JAR, curl for healthcheck.

---

## 3. Infrastructure (reference)

| Component | Technology | Notes |
|-----------|-----------|-------|
| Database | MongoDB 7 | Docker volume `tobyresume-mongo-data` |
| CI/CD | Jenkins LTS + Docker CLI | Docker volume `tobyresume-jenkins-home` |
| App runtime | Eclipse Temurin JRE 17 | Deployed as Docker container |
| Networking | Docker bridge network | Named `tobyresume-network` |

---

## 4. Environment configuration

**What settings differ per environment?**

- **Development** — `SPRING_PROFILES_ACTIVE=dev`; GraphiQL enabled; relaxed CORS; placeholder OAuth2 credentials.
- **Production** — `SPRING_PROFILES_ACTIVE=prod`; JSON logging; health details `when-authorized`; real OAuth2 credentials and JWT secret.

See `.env.example` for all required variables (MongoDB, JWT, OAuth2, CORS origins).

---

## 5. Deployment steps (release process)

### With Jenkins (recommended)

1. Push code to Git (branch configured in Jenkins job).
2. Open Jenkins → click **Build Now**.
3. Pipeline runs: checkout → stop app → migrations → build → deploy → health check.
4. Verify: check Jenkins console output; visit http://localhost:8080/actuator/health.

### Manual (without Jenkins)

1. Build image: `docker build -t tobyresume-app .`
2. Stop old container: `docker stop tobyresume-app && docker rm tobyresume-app` (if running)
3. Run: `docker run -d --name tobyresume-app --network tobyresume-network -p 8080:8080 --env-file .env -e MONGODB_URI=mongodb://tobyresume-mongo:27017/tobyresume tobyresume-app`
4. Verify: `curl http://localhost:8080/actuator/health`

---

## 6. Database

- **MongoDB** — No schema migrations required (documents are flexible). Optional migration scripts in `deploy/db-migrations/` for indexes, seed data, or structural changes.
- **Backup:** Use `mongodump` / cloud provider backups.
- **Restore:** Use `mongorestore` for rollback if needed.

---

## 7. Secrets management

- Use **environment variables** for `JWT_SECRET`, OAuth2 client secrets, `ADMIN_EMAIL`, etc.
- The Jenkins pipeline stores env vars in `$JENKINS_HOME/tobyresume.env` (persisted in Docker volume).
- Do **not** commit secrets. `.env.example` lists required vars with placeholders.
- For production: use Jenkins credentials store or a secrets manager.

---

## 8. Rollback plan

- **Trigger:** Failed health check, critical errors, or failed smoke tests after deploy.
- **Steps:**
  1. Jenkins keeps previous Docker images (tagged by build number).
  2. To rollback: `docker stop tobyresume-app && docker rm tobyresume-app`
  3. Run previous version: `docker run -d --name tobyresume-app ... tobyresume-app:<previous-build-number>`
  4. If data was changed by migrations, restore DB from backup.
- **Communication:** Notify stakeholders if rollback is executed.

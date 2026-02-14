# Local Deployment — Docker, Jenkins, One-Click Deploy

This guide walks through running the Toby.Resume backend and Jenkins on your machine using Docker.

---

## Prerequisites

- **Docker** and **Docker Compose** (v2) installed and running.
- **Git** (for Jenkins to clone the repo).
- Optional: **Java 17** and **Maven** on the host if you want to build without Docker.

---

## 1. Start infrastructure (MongoDB + Jenkins)

From the project root:

```powershell
.\deploy\scripts\start-infra.ps1
```

Or manually:

```bash
docker compose up -d
```

This starts:

| Service | Container | URL / Port |
|---------|-----------|------------|
| MongoDB | tobyresume-mongo | localhost:27017 |
| Jenkins | tobyresume-jenkins | http://localhost:8081 |

Both run on the `tobyresume-network` Docker network.

To start **only MongoDB** (for local dev without Jenkins):

```bash
docker compose up -d mongo
```

---

## 2. Configure Jenkins (first time only)

1. Open http://localhost:8081.
2. Install suggested plugins when prompted (Pipeline and Git are required).
3. Create admin user or continue as admin.
4. **New Item** → name: `tobyresume-backend` → **Pipeline** → OK.
5. **Pipeline** section:
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** your repo URL (e.g. `https://github.com/your-org/BE.git`)
   - **Credentials:** add if the repo is private
   - **Branch:** `*/main` (or your default branch)
   - **Script Path:** `Jenkinsfile`
6. **Save.**

> **Note:** The setup wizard is disabled by default (`runSetupWizard=false` in docker-compose). If you need the wizard, remove that environment variable and restart Jenkins.

---

## 3. One-click deploy

Click **Build Now** in Jenkins. The pipeline runs:

```
Checkout → Stop App → DB Migration → Build → Deploy → Health Check
```

| Stage | What happens |
|-------|-------------|
| **Checkout** | Fetches latest code from Git |
| **Stop App** | Stops `tobyresume-app` container (if running) |
| **DB Migration** | Runs new `.js` scripts from `deploy/db-migrations/` |
| **Build** | `docker build` — multi-stage (Maven build + JRE runtime) |
| **Deploy** | `docker run` — starts app on `tobyresume-network` |
| **Health Check** | Polls `/actuator/health` until UP (max 150s) |

After success:
- **App:** http://localhost:8080
- **Health:** http://localhost:8080/actuator/health
- **GraphQL (dev):** http://localhost:8080/graphql
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

## 4. Environment variables

On **first deploy**, the pipeline creates `$JENKINS_HOME/tobyresume.env` from `.env.example`. This file persists in the Jenkins volume.

To edit (after first deploy):

```bash
docker exec -it tobyresume-jenkins bash
vi /var/jenkins_home/tobyresume.env
```

Or from the host (find the volume path):

```bash
docker volume inspect tobyresume-jenkins-home
# Edit the file at the Mountpoint path
```

Key variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |
| `JWT_SECRET` | JWT signing key (min 32 chars) | dev placeholder |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID | dev placeholder |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 secret | dev placeholder |
| `GITHUB_CLIENT_ID` | GitHub OAuth2 client ID | dev placeholder |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth2 secret | dev placeholder |
| `ADMIN_EMAIL` | Allowed admin email | placeholder@local |
| `ADMIN_PANEL_URL` | Admin panel URL | http://localhost:3000 |
| `ADMIN_PANEL_ORIGIN` | Admin CORS origin | http://localhost:3000 |
| `LANDING_PAGE_ORIGIN` | Landing page CORS origin | http://localhost:3001 |

> `MONGODB_URI` is set automatically by the pipeline (points to `tobyresume-mongo:27017`). You do not need to set it in the env file.

---

## 5. Database migrations

Place MongoDB migration scripts in `deploy/db-migrations/`:

```
deploy/db-migrations/
  V001__create_indexes.js
  V002__seed_default_settings.js
```

The pipeline automatically detects and runs new scripts. See [deploy/db-migrations/README.md](../../../deploy/db-migrations/README.md) for details.

---

## 6. Development mode (no Jenkins)

For quick local development, run MongoDB + App together:

```powershell
.\deploy\scripts\deploy-dev.ps1
```

Or:

```bash
docker compose -f docker-compose.dev.yml up -d --build
```

Stop:

```powershell
.\deploy\scripts\down.ps1 -Dev
```

You can also run just MongoDB and start the app from your IDE:

```bash
docker compose up -d mongo
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 7. Test after deploy

Run the smoke-test script:

```powershell
.\deploy\scripts\test-after-deploy.ps1
```

Or follow the manual checklist: **[Test after deploy](test-after-deploy.md)**.

---

## 8. Stop everything

```powershell
.\deploy\scripts\down.ps1
```

This stops MongoDB, Jenkins, **and** the app container (if deployed by Jenkins).

---

## 9. Quick reference

| Goal | Command |
|------|---------|
| Start infrastructure | `.\deploy\scripts\start-infra.ps1` or `docker compose up -d` |
| Deploy app | Jenkins → **Build Now** |
| Dev mode (no Jenkins) | `.\deploy\scripts\deploy-dev.ps1` |
| Test after deploy | `.\deploy\scripts\test-after-deploy.ps1` |
| Stop everything | `.\deploy\scripts\down.ps1` |
| Stop dev stack | `.\deploy\scripts\down.ps1 -Dev` |
| App health | http://localhost:8080/actuator/health |
| GraphQL | http://localhost:8080/graphql (dev profile) |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Jenkins | http://localhost:8081 |

---

## 10. Troubleshooting

- **Jenkins build fails with "docker: command not found"**
  The custom Jenkins image (`deploy/jenkins/Dockerfile`) includes Docker CLI. Make sure you built the image: `docker compose up -d --build`.

- **Jenkins cannot connect to Docker**
  Ensure the Docker socket is mounted (`/var/run/docker.sock`) and Jenkins runs as root. Check `docker-compose.yml` has `user: root` for the jenkins service.

- **App cannot connect to MongoDB**
  The pipeline sets `MONGODB_URI=mongodb://tobyresume-mongo:27017/tobyresume`. Ensure both containers are on `tobyresume-network`: `docker network inspect tobyresume-network`.

- **Health check fails after deploy**
  Check container logs: `docker logs tobyresume-app`. Common causes: missing env vars, MongoDB not healthy, port conflict.

- **OAuth2 / JWT not working**
  Edit `$JENKINS_HOME/tobyresume.env` with real OAuth2 credentials and a JWT secret (min 32 characters). Redeploy via Jenkins.

- **Port 8080 or 8081 already in use**
  Stop the conflicting process or change the port mapping in `docker-compose.yml` / `Jenkinsfile`.

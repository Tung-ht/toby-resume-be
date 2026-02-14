# Test after deploy

Run these checks once the stack is up to confirm the backend is live.

## Quick (script)

From repo root:

```powershell
.\deploy\scripts\test-after-deploy.ps1
```

Optional base URL: `.\deploy\scripts\test-after-deploy.ps1 -BaseUrl "http://localhost:8080"`

The script checks: **actuator health**, **GraphQL** `siteSettings`, **Swagger UI**.

---

## Manual checklist

### 1. Health

```bash
curl -s http://localhost:8080/actuator/health
```

Expected: `{"status":"UP",...}` (or `UP` inside JSON).

### 2. GraphQL (public)

```bash
curl -s -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d "{\"query\":\"query { siteSettings { siteName } }\"}"
```

Expected: `{"data":{"siteSettings":{...}}}` (no GraphQL errors).

### 3. Swagger UI

Open in browser: **http://localhost:8080/swagger-ui.html**

Expected: Swagger UI loads and shows API docs.

### 4. Optional — protected REST (needs JWT)

Endpoints under `/api/v1/*` require a valid JWT. Without a token you should get **401 Unauthorized**:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/hero
# Expected: 401
```

To test with auth: obtain a JWT via OAuth2 login (or your auth flow), then:

```bash
curl -s -H "Authorization: Bearer YOUR_JWT" http://localhost:8080/api/v1/hero
```

---

## If something fails

- **Health down** — Check app container: `docker compose ps` and `docker compose logs app`. Ensure MongoDB is healthy and `MONGODB_URI` is correct (e.g. `mongodb://mongo:27017/tobyresume` in Docker).
- **GraphQL 404/500** — Confirm app is on port 8080 and profile allows `/graphql` (dev/profile).
- **Swagger 404** — Same as above; ensure no reverse proxy is stripping path.

See [local-deployment.md](local-deployment.md) §6 Troubleshooting and [README.md](README.md) for rollback.

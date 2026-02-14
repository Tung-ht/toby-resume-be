---
phase: frontend
title: FrontEnd — API Reference
description: Complete REST and GraphQL API reference for Admin Panel and Landing Page
---

# FrontEnd — API Reference

This document is the **API reference for frontend developers** building the Admin Panel (REST) and Landing Page (GraphQL). Base URLs and shapes match the backend design and implementation.

**Backend design:** `docs/ai/design/api-design.md`, `docs/ai/design/phase1-mvp.md`.

---

## 1. Base URLs and Conventions

| Environment | REST base | GraphQL endpoint |
|-------------|-----------|------------------|
| Local dev | `http://localhost:8080` | `http://localhost:8080/graphql` |
| Production | Configured API host (e.g. `https://api.tobyresume.com`) | `{API_HOST}/graphql` |

- **REST prefix:** All admin endpoints use `/api/v1/`.
- **GraphQL:** Single endpoint `POST /graphql`; no path version.
- **CORS:** Backend allows configured origins (Admin Panel and Landing Page). Send `Content-Type: application/json` and, for REST admin, `Authorization: Bearer <jwt>`.

### 1.1 REST Response Envelope

Every REST response has this shape:

**Success (200):**
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

**Error (4xx/5xx):**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      { "field": "hero.tagline", "message": "Max length 500" }
    ]
  },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

- Always check `success` first. On error, use `error.code` and `error.message`; use `error.details` for field-level validation (400).
- `timestamp` is ISO-8601 UTC.

### 1.2 REST Error Codes and HTTP Status

| Code | HTTP Status | Action (suggested) |
|------|-------------|--------------------|
| `VALIDATION_ERROR` | 400 | Show `message` and map `details` to form fields. |
| `INVALID_LOCALE` | 400 | Show message; ensure only `en`/`vi` are used. |
| `UNAUTHORIZED` | 401 | Clear token; redirect to login. |
| `FORBIDDEN` | 403 | Clear token; redirect to login. |
| `RESOURCE_NOT_FOUND` | 404 | Show “Not found”; navigate to list or 404 page. |
| `PUBLISH_FAILED` | 500 | Show “Publish failed”; offer retry. |
| `INTERNAL_ERROR` | 500 | Show generic error; optionally retry. |
| `RATE_LIMITED` | 429 | Show “Too many requests”; retry after delay. |

---

## 2. REST API (Admin Panel)

All endpoints under `/api/v1/*` require: `Authorization: Bearer <jwt>`.

### 2.1 Authentication

#### OAuth2 (browser redirect)

| Provider | URL (GET, same origin as API) |
|----------|-------------------------------|
| Google | `/oauth2/authorization/google` |
| GitHub | `/oauth2/authorization/github` |

- Frontend: navigate user to `{API_BASE}/oauth2/authorization/google` (or `github`).
- After consent, backend redirects to `{ADMIN_PANEL_URL}/auth/callback?token=<jwt>` or `?error=access_denied`.
- Frontend stores the token and uses it for subsequent REST calls.

#### GET /api/v1/auth/me

Returns current user. **Headers:** `Authorization: Bearer <jwt>`.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "email": "toby@example.com",
    "name": "Toby Nguyen",
    "role": "ADMIN",
    "provider": "google"
  },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

#### POST /api/v1/auth/logout

Optional; backend may not invalidate token in MVP. **Headers:** `Authorization: Bearer <jwt>`. Client should clear token and redirect to login.

**Response 200:** Body may be empty or envelope with `data: null`.

---

### 2.2 Hero (Singleton)

#### GET /api/v1/hero

**Response 200:** `data` is hero object or `null` if no draft yet.

```json
{
  "success": true,
  "data": {
    "tagline": { "en": "...", "vi": "..." },
    "bio": { "en": "...", "vi": "..." },
    "fullName": { "en": "...", "vi": "..." },
    "title": { "en": "...", "vi": "..." },
    "profilePhotoMediaId": null,
    "updatedAt": "2026-02-11T10:30:00Z"
  },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

#### PUT /api/v1/hero

**Request body:**
```json
{
  "tagline": { "en": "Full Stack Developer", "vi": "Lập trình viên Full Stack" },
  "bio": { "en": "Markdown bio...", "vi": "Bio tiếng Việt..." },
  "fullName": { "en": "Toby Nguyen", "vi": "Nguyễn Toby" },
  "title": { "en": "Software Engineer", "vi": "Kỹ sư phần mềm" },
  "profilePhotoMediaId": null
}
```

| Field | Type | Validation |
|-------|------|------------|
| tagline | `Map<string, string>` | Keys: en, vi. Max 500 chars per value. |
| bio | `Map<string, string>` | Max 2000 per value. |
| fullName, title | `Map<string, string>` | Max 200 per value. |
| profilePhotoMediaId | `string \| null` | Optional. |

**Response 200:** Same envelope; `data` = saved hero (includes `updatedAt`).

---

### 2.3 Work Experience (List)

Base path: `/api/v1/experiences`.

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/experiences | List all draft items (sorted by order). |
| GET | /api/v1/experiences/{itemId} | Get one item. 404 if not found. |
| POST | /api/v1/experiences | Add item. |
| PUT | /api/v1/experiences/{itemId} | Update item. |
| DELETE | /api/v1/experiences/{itemId} | Delete item. |
| PUT | /api/v1/experiences/reorder | Reorder items. |

**POST body (add):**
```json
{
  "company": { "en": "TechCorp", "vi": "TechCorp" },
  "role": { "en": "Senior Engineer", "vi": "Kỹ sư cao cấp" },
  "startDate": "2023-01",
  "endDate": null,
  "bulletPoints": {
    "en": ["Led a team of 5.", "Built microservices."],
    "vi": ["Dẫn dắt đội 5.", "Xây dựng microservices."]
  },
  "techUsed": ["Java", "Spring Boot", "MongoDB"],
  "order": 0
}
```

- `company`, `role`: required (at least one locale); max 200 chars per value.
- `startDate`: required, `YYYY-MM`.
- `endDate`: `YYYY-MM` or null.
- `bulletPoints`: max 10 per locale; each item max 500 chars.
- `order`: optional; server can assign if omitted.

**PUT reorder body:**
```json
{
  "orderedIds": ["uuid-1", "uuid-2", "uuid-3"]
}
```

---

### 2.4 Projects (List)

Base path: `/api/v1/projects`. Same pattern as Experiences (GET list, GET one, POST, PUT, DELETE, PUT reorder).

**POST body (add):**
- `title`: `Map<string, string>`, max 200 per value.
- `description`: `Map<string, string>`, Markdown, max 3000 per value.
- `techStack`: `string[]`.
- `links`: `Array<{ label: string, url: string }>`, max 10.
- `mediaIds`: `string[]`, max 10 (optional).
- `visible`: boolean, default true.
- `order`: number.

---

### 2.5 Education (List)

Base path: `/api/v1/education`. Same CRUD + reorder pattern.

**POST/PUT body:** `institution`, `degree`, `field` (strings, max 200); `startDate`, `endDate` (YYYY-MM); `details` (Map en/vi, max 1000 per value); `order`.

---

### 2.6 Skills (Categories + Items)

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/skills | List all categories with items. |
| POST | /api/v1/skills | Add category. |
| PUT | /api/v1/skills/{categoryId} | Update category (name + items). |
| DELETE | /api/v1/skills/{categoryId} | Delete category. |
| PUT | /api/v1/skills/reorder | Reorder categories. |

**Category:** `name` = `Map<string, string>` (max 100 per locale). **Items:** array of `{ name: string, level?: string }`; max 50 items per category.

**PUT reorder body:** `{ "orderedIds": ["categoryId1", "categoryId2", ...] }`.

---

### 2.7 Certifications (List)

Base path: `/api/v1/certifications`. Same list CRUD + reorder pattern.

**POST/PUT body:** `title`, `issuer` (max 200); `date` (string); `url` (optional); `description` (Map en/vi, optional, max 500 per value); `order`.

---

### 2.8 Social Links (List)

Base path: `/api/v1/social-links`. Same list CRUD + reorder pattern.

**POST/PUT body:** `platform` (max 50), `url` (valid URL), `icon` (optional), `order`. No localized fields.

---

### 2.9 Preview (Draft Payload)

#### GET /api/v1/preview

**Query params:** `locale` (optional): `en` or `vi`. If set, all localized fields in the response are single-locale strings for that locale.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "hero": { "tagline": "...", "bio": "...", "fullName": "...", "title": "...", "profilePhotoUrl": null },
    "experiences": [
      {
        "id": "uuid-1",
        "company": "TechCorp",
        "role": "Senior Engineer",
        "startDate": "2023-01",
        "endDate": null,
        "bulletPoints": [],
        "techUsed": [],
        "order": 0
      }
    ],
    "projects": [],
    "education": [],
    "skills": [],
    "certifications": [],
    "socialLinks": []
  },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

- Use this payload to render the same layout as the Landing Page for draft preview. With `?locale=en`, each field is already resolved to that locale.

---

### 2.10 Publish

#### POST /api/v1/publish

**Body:** Optional `{ "label": "v1.0" }` (if supported). No body required.

**Response 200:**
```json
{
  "success": true,
  "data": {
    "publishedAt": "2026-02-11T12:00:00Z",
    "versionId": "507f1f77bcf86cd799439011",
    "sectionsPublished": ["hero", "experiences", "projects", "education", "skills", "certifications", "socialLinks"]
  },
  "timestamp": "2026-02-11T12:00:00Z"
}
```

#### GET /api/v1/publish/status

**Response 200:**
```json
{
  "success": true,
  "data": {
    "lastPublishedAt": "2026-02-11T12:00:00Z",
    "versionCount": 3
  },
  "timestamp": "2026-02-11T12:05:00Z"
}
```

- If never published: `lastPublishedAt` may be null, `versionCount` 0.

---

### 2.11 Settings

#### GET /api/v1/settings

**Response 200:** `data` = site settings object.

#### PUT /api/v1/settings

**Request body:**
```json
{
  "supportedLocales": ["en", "vi"],
  "defaultLocale": "en",
  "pdfSectionVisibility": {
    "hero": true,
    "experiences": true,
    "projects": true,
    "education": true,
    "skills": true,
    "certifications": true,
    "socialLinks": false
  }
}
```

- `supportedLocales`: fixed `["en", "vi"]` in Phase 1.
- `defaultLocale`: must be one of supportedLocales.
- `pdfSectionVisibility`: keys as above; boolean per section.

---

## 3. GraphQL API (Landing Page)

- **Endpoint:** `POST /graphql`
- **Auth:** None. Only **published** content is exposed.
- **Content-Type:** `application/json`.

**Request body:**
```json
{
  "query": "query LandingPage($locale: Locale!) { ... }",
  "variables": { "locale": "EN" }
}
```

### 3.1 Schema Summary

- **Enum:** `Locale`: `EN`, `VI`.
- **Query root:** `hero(locale)`, `experiences(locale)`, `projects(locale)`, `education(locale)`, `skills(locale)`, `certifications(locale)`, `socialLinks`, `siteSettings`.
- **Locale:** When provided (e.g. `EN`), resolvers return that locale’s value for localized fields. When omitted, backend uses `siteSettings.defaultLocale`.
- **Ordering:** All lists sorted by `order` ascending. **Projects:** only `visible: true` returned.

### 3.2 Types (for reference)

| Type | Fields |
|------|--------|
| Hero | tagline, bio, fullName, title, profilePhotoUrl (all String or null) |
| ExperienceItem | id, company, role, startDate, endDate, bulletPoints, techUsed, order |
| ProjectItem | id, title, description, techStack, links, mediaUrls, visible, order |
| Link | label, url |
| EducationItem | id, institution, degree, field, startDate, endDate, details, order |
| SkillCategory | id, name, items, order |
| SkillItem | name, level |
| CertificationItem | id, title, issuer, date, url, description, order |
| SocialLinkItem | id, platform, url, icon, order |
| SiteSettings | supportedLocales, defaultLocale |

### 3.3 Example Query (Landing Page)

```graphql
query LandingPage($locale: Locale!) {
  hero(locale: $locale) {
    fullName
    title
    tagline
    bio
    profilePhotoUrl
  }
  experiences(locale: $locale) {
    id
    company
    role
    startDate
    endDate
    bulletPoints
    techUsed
    order
  }
  projects(locale: $locale) {
    id
    title
    description
    techStack
    links { label url }
    mediaUrls
    visible
    order
  }
  education(locale: $locale) {
    id
    institution
    degree
    field
    startDate
    endDate
    details
    order
  }
  skills(locale: $locale) {
    id
    name
    items { name level }
    order
  }
  certifications(locale: $locale) {
    id
    title
    issuer
    date
    url
    description
    order
  }
  socialLinks {
    id
    platform
    url
    icon
    order
  }
  siteSettings {
    supportedLocales
    defaultLocale
  }
}
```

**Variables:** `{ "locale": "EN" }` or `{ "locale": "VI" }`.

### 3.4 GraphQL Error Response

- Check `errors` array in the response. Each error has `message`, `path`, and optionally `extensions.code`.
- Do not expose internal details to the user. Show a generic “Unable to load content” and optionally retry.
- Backend may return partial data with errors (e.g. hero null, rest filled).

**Example:**
```json
{
  "data": { "hero": null },
  "errors": [
    {
      "message": "No published content found",
      "path": ["hero"],
      "extensions": { "code": "NOT_FOUND" }
    }
  ]
}
```

---

## 4. REST Endpoint Quick Reference

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /oauth2/authorization/google | No | Start Google OAuth2 |
| GET | /oauth2/authorization/github | No | Start GitHub OAuth2 |
| GET | /api/v1/auth/me | JWT | Current user |
| POST | /api/v1/auth/logout | JWT | Logout |
| GET | /api/v1/hero | JWT | Get draft hero |
| PUT | /api/v1/hero | JWT | Save draft hero |
| GET | /api/v1/experiences | JWT | List experiences |
| GET | /api/v1/experiences/{itemId} | JWT | Get one experience |
| POST | /api/v1/experiences | JWT | Add experience |
| PUT | /api/v1/experiences/{itemId} | JWT | Update experience |
| DELETE | /api/v1/experiences/{itemId} | JWT | Delete experience |
| PUT | /api/v1/experiences/reorder | JWT | Reorder experiences |
| GET/POST/GET one/PUT/DELETE/PUT reorder | /api/v1/projects, /api/v1/education, /api/v1/certifications, /api/v1/social-links | JWT | Same pattern as experiences |
| GET/POST/PUT/DELETE | /api/v1/skills, /api/v1/skills/{categoryId} | JWT | Skills categories |
| PUT | /api/v1/skills/reorder | JWT | Reorder categories |
| GET | /api/v1/preview?locale=en | JWT | Full draft payload |
| POST | /api/v1/publish | JWT | Publish draft → live |
| GET | /api/v1/publish/status | JWT | Last publish info |
| GET | /api/v1/settings | JWT | Get settings |
| PUT | /api/v1/settings | JWT | Update settings |

---

## 5. Document Cross-References

- **Backend API design (full):** `docs/ai/design/api-design.md`
- **System design:** `docs/ai/design/phase1-mvp.md`
- **API conventions:** `docs/ai/requirements/feature-api-conventions.md`
- **UI/UX requirements:** `docs/ai/frontend/ui-ux-requirements.md`

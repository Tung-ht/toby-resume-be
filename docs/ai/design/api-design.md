---
phase: design
title: "API Design — Complete REST & GraphQL Reference"
description: "All endpoints, request/response contracts, auth, error handling, and conventions for Phase 1 MVP"
---

# API Design — Complete REST & GraphQL Reference

> **Base URL (REST):** `https://api.example.com` (or `http://localhost:8080` in dev)  
> **REST prefix:** `/api/v1/`  
> **GraphQL endpoint:** `POST /graphql`  
> **Auth:** JWT for admin REST; none for GraphQL (public read-only)

---

## 1. API Overview

| API | Purpose | Auth | Consumers |
|-----|---------|------|-----------|
| **REST** | Admin CRUD, preview, publish, settings, auth | JWT (Bearer) | Admin Panel |
| **GraphQL** | Public read-only content for landing page | None | Landing Page |
| **OAuth2** | Login (Google, GitHub) → JWT | None (redirect flow) | Admin Panel |

### 1.1 Versioning

- **REST:** All admin endpoints use path prefix `/api/v1/`. Future versions may use `/api/v2/`.
- **GraphQL:** Single endpoint `/graphql` for v1; schema version may be added in response or header later.

### 1.2 CORS

- **Allowed origins:** Configured via `app.cors.allowed-origins` (Admin Panel + Landing Page URLs).
- **Methods:** `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`.
- **Headers:** `Content-Type`, `Authorization`.
- **Credentials:** `true` (cookies/session if needed for OAuth2 callback).

---

## 2. REST Response Envelope

All REST responses use a **unified envelope**. Client should check `success` first.

### 2.1 Success Response

```json
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-02-11T10:30:00Z"
}
```

- `data` contains the resource or result. Shape depends on endpoint.
- `timestamp` is ISO-8601 UTC.

### 2.2 Error Response

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

- `error.code`: Machine-readable code (see Error Codes table).
- `error.message`: Human-readable summary.
- `error.details`: Optional array of field-level errors (for 400 validation).
- On error, `data` is omitted.

### 2.3 Error Codes & HTTP Status

| Code | HTTP Status | When |
|------|-------------|------|
| `VALIDATION_ERROR` | 400 | Invalid input, missing required fields, constraint violation |
| `INVALID_LOCALE` | 400 | Locale key not in `["en", "vi"]` |
| `UNAUTHORIZED` | 401 | Missing or invalid JWT |
| `FORBIDDEN` | 403 | Authenticated but not in allowed-admins list |
| `RESOURCE_NOT_FOUND` | 404 | Section or item not found |
| `PUBLISH_FAILED` | 500 | Publish pipeline error |
| `INTERNAL_ERROR` | 500 | Unexpected server error |
| `RATE_LIMITED` | 429 | Phase 2+ — rate limit exceeded |

---

## 3. Authentication

### 3.1 OAuth2 Login (No JWT Yet)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/oauth2/authorization/google` | No | Redirect to Google consent; after approval, callback returns to app with JWT |
| `GET` | `/oauth2/authorization/github` | No | Redirect to GitHub consent; same flow |

**Flow:**

1. Admin Panel redirects user to `GET /oauth2/authorization/google` (or `github`).
2. User approves on provider; provider redirects to backend callback with `code`.
3. Backend exchanges `code` for tokens, loads user info, checks email/id against `app.security.allowed-admins`.
4. If allowed: backend generates JWT and redirects to `{ADMIN_PANEL_URL}/auth/callback?token=<jwt>` (or sets HTTP-only cookie).
5. If not allowed: redirect to Admin Panel with `?error=forbidden` (or similar).

### 3.2 JWT Usage for REST

- **Header:** `Authorization: Bearer <jwt>`.
- **Required for:** All `/api/v1/*` endpoints except those used during OAuth2 redirect.
- **Expiry:** 24 hours (configurable via `app.security.jwt.expiration-ms`).

**JWT payload (claims):**

```json
{
  "sub": "toby@example.com",
  "name": "Toby Nguyen",
  "provider": "google",
  "role": "ADMIN",
  "iat": 1739271600,
  "exp": 1739358000
}
```

### 3.3 Auth Endpoints (REST)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/auth/me` | JWT | Returns current user info (email, name, role). |
| `POST` | `/api/v1/auth/logout` | JWT | Client-side: discard token. Server may blacklist in Phase 2. |

**GET /api/v1/auth/me — Response (200):**

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

---

## 4. REST API — Content CRUD

All content endpoints operate on **DRAFT** state only. Require **JWT**.

### 4.1 Hero (Singleton)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/hero` | Get draft hero. Returns 200 with body or 200 with `data: null` if no draft yet. |
| `PUT` | `/api/v1/hero` | Create or update draft hero. Idempotent; full replace. |

**PUT /api/v1/hero — Request body:**

```json
{
  "tagline": { "en": "Full Stack Developer", "vi": "Lập trình viên Full Stack" },
  "bio": { "en": "Markdown bio...", "vi": "Bio tiếng Việt..." },
  "fullName": { "en": "Toby Nguyen", "vi": "Nguyễn Toby" },
  "title": { "en": "Software Engineer", "vi": "Kỹ sư phần mềm" },
  "profilePhotoMediaId": null
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `tagline` | `Map<String, String>` | No | Keys: en, vi. Max 500 chars per value. |
| `bio` | `Map<String, String>` | No | Max 2000 chars per value. |
| `fullName` | `Map<String, String>` | No | Max 200 chars per value. |
| `title` | `Map<String, String>` | No | Max 200 chars per value. |
| `profilePhotoMediaId` | `String` or `null` | No | Reference to media (Phase 2). |

**Response (200):** Same envelope; `data` is the saved hero (includes `updatedAt`).

---

### 4.2 Work Experience (List)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/experiences` | List all draft experience items, sorted by `order` ascending. |
| `GET` | `/api/v1/experiences/{itemId}` | Get one experience item by `itemId`. 404 if not found. |
| `POST` | `/api/v1/experiences` | Add new item; server assigns `itemId` and default `order`. |
| `PUT` | `/api/v1/experiences/{itemId}` | Update item. 404 if not found. |
| `DELETE` | `/api/v1/experiences/{itemId}` | Delete item. 404 if not found. |
| `PUT` | `/api/v1/experiences/reorder` | Reorder items. Body: `{ "orderedIds": ["id1", "id2", ...] }`. |

**POST /api/v1/experiences — Request body:**

```json
{
  "company": { "en": "TechCorp", "vi": "TechCorp" },
  "role": { "en": "Senior Engineer", "vi": "Kỹ sư cao cấp" },
  "startDate": "2023-01",
  "endDate": null,
  "bulletPoints": {
    "en": ["Led a team of 5 engineers.", "Built microservices."],
    "vi": ["Dẫn dắt đội 5 kỹ sư.", "Xây dựng microservices."]
  },
  "techUsed": ["Java", "Spring Boot", "MongoDB"],
  "order": 0
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `company` | `Map<String, String>` | Yes (≥1 locale) | Max 200 chars per value. |
| `role` | `Map<String, String>` | Yes (≥1 locale) | Max 200 chars per value. |
| `startDate` | `String` | Yes | `YYYY-MM`. |
| `endDate` | `String` or `null` | No | `YYYY-MM` or null. |
| `bulletPoints` | `Map<String, List<String>>` | No | Max 10 per locale; each item max 500 chars. |
| `techUsed` | `String[]` | No | — |
| `order` | `int` | No | Default last. |

**PUT /api/v1/experiences/reorder — Request body:**

```json
{
  "orderedIds": [
    "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "b2c3d4e5-f6a7-8901-bcde-f12345678901"
  ]
}
```

**Response (200):** `data` can be the updated list or `{ "reordered": true }` per implementation choice.

---

### 4.3 Projects (List)

Same pattern as Work Experience.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/projects` | List all draft project items by `order`. |
| `GET` | `/api/v1/projects/{itemId}` | Get one project item. |
| `POST` | `/api/v1/projects` | Add project item. |
| `PUT` | `/api/v1/projects/{itemId}` | Update project item. |
| `DELETE` | `/api/v1/projects/{itemId}` | Delete project item. |
| `PUT` | `/api/v1/projects/reorder` | Body: `{ "orderedIds": ["id1", "id2", ...] }`. |

**POST /api/v1/projects — Request body (key fields):**

- `title`: `Map<String, String>`, max 200 per value.
- `description`: `Map<String, String>`, Markdown, max 3000 per value.
- `techStack`: `String[]`.
- `links`: `Array<{ label: string, url: string }>`, max 10.
- `mediaIds`: `String[]`, max 10 (Phase 2).
- `visible`: `boolean`, default `true`.
- `order`: `int`.

---

### 4.4 Education (List)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/education` | List draft education items. |
| `GET` | `/api/v1/education/{itemId}` | Get one item. |
| `POST` | `/api/v1/education` | Add item. |
| `PUT` | `/api/v1/education/{itemId}` | Update item. |
| `DELETE` | `/api/v1/education/{itemId}` | Delete item. |
| `PUT` | `/api/v1/education/reorder` | Body: `{ "orderedIds": [...] }`. |

**Request body (POST/PUT):** `institution`, `degree`, `field` (strings, max 200); `startDate`, `endDate` (YYYY-MM); `details` (Map en/vi, max 1000 per value); `order` (int).

---

### 4.5 Skills (Categories + Items)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/skills` | List all draft skill categories with items, by category `order`. |
| `POST` | `/api/v1/skills` | Add skill category (name per locale, items array). |
| `PUT` | `/api/v1/skills/{categoryId}` | Update category (name + items). |
| `DELETE` | `/api/v1/skills/{categoryId}` | Delete category. |
| `PUT` | `/api/v1/skills/reorder` | Body: `{ "orderedIds": ["categoryId1", ...] }`. |

**Category:** `name` = `Map<String, String>` (max 100 per locale). **Item:** `name` (string, max 100), `level` (string, optional). Max 50 items per category.

---

### 4.6 Certifications (List)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/certifications` | List draft certification items. |
| `GET` | `/api/v1/certifications/{itemId}` | Get one item. |
| `POST` | `/api/v1/certifications` | Add item. |
| `PUT` | `/api/v1/certifications/{itemId}` | Update item. |
| `DELETE` | `/api/v1/certifications/{itemId}` | Delete item. |
| `PUT` | `/api/v1/certifications/reorder` | Body: `{ "orderedIds": [...] }`. |

**Request body:** `title`, `issuer` (max 200); `date` (string); `url` (optional); `description` (Map en/vi, optional, max 500 per value); `order` (int).

---

### 4.7 Social Links (List)

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/social-links` | List draft social link items. |
| `GET` | `/api/v1/social-links/{itemId}` | Get one item. |
| `POST` | `/api/v1/social-links` | Add item. |
| `PUT` | `/api/v1/social-links/{itemId}` | Update item. |
| `DELETE` | `/api/v1/social-links/{itemId}` | Delete item. |
| `PUT` | `/api/v1/social-links/reorder` | Body: `{ "orderedIds": [...] }`. |

**Request body:** `platform` (max 50), `url` (valid URL), `icon` (optional), `order` (int). No localized fields.

---

## 5. REST API — Preview & Publish

### 5.1 Preview (Draft Payload for Admin)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/preview` | JWT | All draft sections in one payload (for preview page). |
| `GET` | `/api/v1/preview?locale=en` | JWT | Same, with content filtered to single locale. |

**Query parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `locale` | `string` | No | `en` or `vi`. If omitted, returns all locales (same shape as draft DB). |

**Response (200):**

```json
{
  "success": true,
  "data": {
    "hero": {
      "tagline": { "en": "...", "vi": "..." },
      "bio": { "en": "...", "vi": "..." },
      "fullName": { "en": "...", "vi": "..." },
      "title": { "en": "...", "vi": "..." },
      "profilePhotoUrl": null
    },
    "experiences": [
      {
        "id": "uuid-1",
        "company": "...",
        "role": "...",
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

When `?locale=en` is used, each field in `data` is the **single-locale value** (e.g. `"company": "TechCorp"`) so the frontend can render the same as GraphQL for that locale.

---

### 5.2 Publish Pipeline

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/api/v1/publish` | JWT | Copy DRAFT → PUBLISHED for all sections; create version snapshot; return metadata. |
| `GET` | `/api/v1/publish/status` | JWT | Last publish time and version count. |

**POST /api/v1/publish — Request body:** None (optional future: `{ "label": "v1.0" }`).

**Response (200):**

```json
{
  "success": true,
  "data": {
    "publishedAt": "2026-02-11T12:00:00Z",
    "versionId": "507f1f77bcf86cd799439011",
    "sectionsPublished": [
      "hero",
      "experiences",
      "projects",
      "education",
      "skills",
      "certifications",
      "socialLinks"
    ]
  },
  "timestamp": "2026-02-11T12:00:00Z"
}
```

**GET /api/v1/publish/status — Response (200):**

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

If never published: `lastPublishedAt` can be `null`, `versionCount` 0.

---

## 6. REST API — Settings

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/api/v1/settings` | JWT | Get site settings. |
| `PUT` | `/api/v1/settings` | JWT | Update site settings. |

**PUT /api/v1/settings — Request body:**

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

| Field | Type | Validation |
|-------|------|------------|
| `supportedLocales` | `String[]` | Fixed `["en", "vi"]` for Phase 1. |
| `defaultLocale` | `String` | Must be in `supportedLocales`. |
| `pdfSectionVisibility` | `Map<String, Boolean>` | Keys: hero, experiences, projects, education, skills, certifications, socialLinks. |

**Response (200):** Same envelope; `data` is the saved settings object.

---

## 7. REST Endpoint Summary Table

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/oauth2/authorization/google` | No | Start Google OAuth2 |
| `GET` | `/oauth2/authorization/github` | No | Start GitHub OAuth2 |
| `GET` | `/api/v1/auth/me` | JWT | Current user |
| `POST` | `/api/v1/auth/logout` | JWT | Logout |
| `GET` | `/api/v1/hero` | JWT | Get draft hero |
| `PUT` | `/api/v1/hero` | JWT | Save draft hero |
| `GET` | `/api/v1/experiences` | JWT | List draft experiences |
| `GET` | `/api/v1/experiences/{itemId}` | JWT | Get one experience |
| `POST` | `/api/v1/experiences` | JWT | Add experience |
| `PUT` | `/api/v1/experiences/{itemId}` | JWT | Update experience |
| `DELETE` | `/api/v1/experiences/{itemId}` | JWT | Delete experience |
| `PUT` | `/api/v1/experiences/reorder` | JWT | Reorder experiences |
| `GET` | `/api/v1/projects` | JWT | List draft projects |
| `GET` | `/api/v1/projects/{itemId}` | JWT | Get one project |
| `POST` | `/api/v1/projects` | JWT | Add project |
| `PUT` | `/api/v1/projects/{itemId}` | JWT | Update project |
| `DELETE` | `/api/v1/projects/{itemId}` | JWT | Delete project |
| `PUT` | `/api/v1/projects/reorder` | JWT | Reorder projects |
| `GET` | `/api/v1/education` | JWT | List draft education |
| `GET` | `/api/v1/education/{itemId}` | JWT | Get one education |
| `POST` | `/api/v1/education` | JWT | Add education |
| `PUT` | `/api/v1/education/{itemId}` | JWT | Update education |
| `DELETE` | `/api/v1/education/{itemId}` | JWT | Delete education |
| `PUT` | `/api/v1/education/reorder` | JWT | Reorder education |
| `GET` | `/api/v1/skills` | JWT | List draft skills |
| `POST` | `/api/v1/skills` | JWT | Add skill category |
| `PUT` | `/api/v1/skills/{categoryId}` | JWT | Update skill category |
| `DELETE` | `/api/v1/skills/{categoryId}` | JWT | Delete skill category |
| `PUT` | `/api/v1/skills/reorder` | JWT | Reorder categories |
| `GET` | `/api/v1/certifications` | JWT | List draft certifications |
| `GET` | `/api/v1/certifications/{itemId}` | JWT | Get one certification |
| `POST` | `/api/v1/certifications` | JWT | Add certification |
| `PUT` | `/api/v1/certifications/{itemId}` | JWT | Update certification |
| `DELETE` | `/api/v1/certifications/{itemId}` | JWT | Delete certification |
| `PUT` | `/api/v1/certifications/reorder` | JWT | Reorder certifications |
| `GET` | `/api/v1/social-links` | JWT | List draft social links |
| `GET` | `/api/v1/social-links/{itemId}` | JWT | Get one social link |
| `POST` | `/api/v1/social-links` | JWT | Add social link |
| `PUT` | `/api/v1/social-links/{itemId}` | JWT | Update social link |
| `DELETE` | `/api/v1/social-links/{itemId}` | JWT | Delete social link |
| `PUT` | `/api/v1/social-links/reorder` | JWT | Reorder social links |
| `GET` | `/api/v1/preview` | JWT | Full draft payload |
| `GET` | `/api/v1/preview?locale=en` | JWT | Draft payload, single locale |
| `POST` | `/api/v1/publish` | JWT | Publish draft → live |
| `GET` | `/api/v1/publish/status` | JWT | Last publish info |
| `GET` | `/api/v1/settings` | JWT | Get settings |
| `PUT` | `/api/v1/settings` | JWT | Update settings |

---

## 8. GraphQL API (Public)

- **Endpoint:** `POST /graphql`
- **Auth:** None. Only **PUBLISHED** content is exposed.
- **Operations:** Queries only (no mutations in Phase 1).

### 8.1 Schema

```graphql
# ── Enums ──

enum Locale {
  EN
  VI
}

# ── Root Query ──

type Query {
  hero(locale: Locale): Hero
  experiences(locale: Locale): [ExperienceItem!]!
  projects(locale: Locale): [ProjectItem!]!
  education(locale: Locale): [EducationItem!]!
  skills(locale: Locale): [SkillCategory!]!
  certifications(locale: Locale): [CertificationItem!]!
  socialLinks: [SocialLinkItem!]!
  siteSettings: SiteSettings
}

# ── Types ──

type Hero {
  tagline: String
  bio: String
  fullName: String
  title: String
  profilePhotoUrl: String
}

type ExperienceItem {
  id: ID!
  company: String
  role: String
  startDate: String
  endDate: String
  bulletPoints: [String!]
  techUsed: [String!]
  order: Int!
}

type ProjectItem {
  id: ID!
  title: String
  description: String
  techStack: [String!]
  links: [Link!]
  mediaUrls: [String!]
  visible: Boolean!
  order: Int!
}

type Link {
  label: String!
  url: String!
}

type EducationItem {
  id: ID!
  institution: String
  degree: String
  field: String
  startDate: String
  endDate: String
  details: String
  order: Int!
}

type SkillCategory {
  id: ID!
  name: String
  items: [SkillItem!]!
  order: Int!
}

type SkillItem {
  name: String!
  level: String
}

type CertificationItem {
  id: ID!
  title: String
  issuer: String
  date: String
  url: String
  description: String
  order: Int!
}

type SocialLinkItem {
  id: ID!
  platform: String!
  url: String!
  icon: String
  order: Int!
}

type SiteSettings {
  supportedLocales: [String!]!
  defaultLocale: String!
}
```

### 8.2 Locale & Ordering Rules

- **Locale:** Argument `locale: Locale` (EN | VI). Resolvers return that locale’s value for localized fields. If omitted, use `siteSettings.defaultLocale`.
- **Ordering:** All list fields return items sorted by `order` ascending.
- **Projects:** Only items with `visible: true` are returned.

### 8.3 Example Queries

**Landing page (single locale):**

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

### 8.4 GraphQL Error Response

- Use GraphQL `errors` array. Do not expose internal details or stack traces.
- Example:

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

- Return partial data when only some fields fail. Implement via custom `DataFetcherExceptionResolver` in Spring for GraphQL.

---

## 9. Public vs Protected Endpoints

| Path Pattern | Auth | Notes |
|--------------|------|-------|
| `POST /graphql` | No | Public; published content only |
| `GET /actuator/health` | No | Public health check |
| `GET /oauth2/**`, `GET /login/oauth2/**` | No | OAuth2 redirect/callback |
| `/api/v1/**` | JWT required | All admin REST |

---

## 10. Document Cross-References

- **Database schemas and validation rules:** `docs/ai/design/database-design.md`
- **System architecture and security details:** `docs/ai/design/phase1-mvp.md`
- **API conventions (errors, versioning, CORS):** `docs/ai/requirements/feature-api-conventions.md`

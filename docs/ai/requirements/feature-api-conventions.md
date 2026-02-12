---
phase: requirements
title: API Conventions — Error Handling, Versioning, CORS, Rate Limiting
description: Cross-cutting REST and GraphQL conventions for Toby.Resume backend APIs
---

# API Conventions — Error Handling, Versioning, CORS, Rate Limiting

This document defines conventions that apply to all backend APIs (REST admin and GraphQL public). Feature-specific docs reference these where relevant.

## Error Handling

### REST

- **Standard error body:** Use a consistent shape for all error responses, e.g.:
  - `{ "error": { "code": "...", "message": "...", "details": optional } }`
  - `details` may be a list of field-level validation errors (see below).
- **HTTP status usage:**
  - **400 Bad Request** — Validation errors (invalid input, missing required fields).
  - **401 Unauthorized** — Missing or invalid auth (admin endpoints); client should re-authenticate.
  - **403 Forbidden** — Authenticated but not allowed (e.g. wrong role).
  - **404 Not Found** — Resource does not exist.
  - **409 Conflict** — Optional; use if optimistic locking or conflict detection is added later.
  - **429 Too Many Requests** — Rate limit exceeded (see Rate Limiting).
  - **500 Internal Server Error** — Unexpected server error; no partial apply or partial state on 5xx.
- **Validation errors:** When returning 400 for validation failures, include a list of field errors so the client can show inline validation, e.g. `details: [ { "field": "hero.tagline", "message": "Max length 500" } ]`.

### GraphQL

- **Errors:** Use the GraphQL errors array for operational and validation errors. Public API has no auth, so no 401; only published data is exposed.
- **Partial data:** Follow GraphQL spec: return partial data with errors where applicable; avoid exposing internal details in error messages.

## API Versioning

- **REST:** URL path prefix (e.g. `/api/v1/...`) for all admin endpoints. v1 is the default; future versions TBD.
- **GraphQL:** Single endpoint (e.g. `/graphql`) for v1; schema version may be included in response or Accept header if needed later. No version in path for initial release.

## CORS

- **Allowed origins:** Admin Panel and Landing Page origins must be explicitly allowed (configured via env or config). Credentials (cookies/session) allowed where needed for admin.
- **Methods:** Allow GET, POST, PUT, PATCH, DELETE, OPTIONS for REST; POST for GraphQL.
- **Headers:** Allow Content-Type, Authorization, and any custom headers required by the frontends. Preflight (OPTIONS) must be supported for REST.

## Rate Limiting

- **REST (admin):** Optional: e.g. 100 requests per minute per authenticated session. Not enforced in v1 unless specified in design.
- **GraphQL (public):** Optional: e.g. 60 requests per minute per IP to protect against abuse. Design phase may define exact limits.
- **Contact form:** Already specified in feature-extras: e.g. 5 submissions per hour per IP; return 429 when exceeded.

Details and exact limits are defined in design docs; this document establishes that rate limiting is part of API design and where it applies.

# Implementation Review — Phase 1 MVP (Phases A, B, C)

**Scope:** Phase A (Foundation), Phase B (Common Layer), Phase C (Security & Auth)  
**Design:** `docs/ai/design/api-design.md`, `database-design.md`, `phase1-mvp.md`  
**Requirements:** `docs/ai/requirements/feature-api-conventions.md`  
**Date:** 2026-02-12

---

## 1. Context

### 1.1 Feature / branch description

Current implementation delivers:

- **Phase A:** Runnable Spring Boot app, MongoDB config, package structure, Actuator, Docker Compose, minimal security (all permit).
- **Phase B:** ContentState, BaseDocument + MongoConfig auditing, ApiResponse/ErrorBody envelope, GlobalExceptionHandler, custom exceptions, @ValidLocaleKeys, CORS from config, IdGenerator.
- **Phase C:** OAuth2 Google + GitHub, JwtTokenProvider (HS256), JwtAuthFilter, OAuth2 success/failure handlers, SecurityConfig (protect `/api/v1/**`, 401/403 JSON), AuthController (GET /auth/me, POST /auth/logout), AccessDeniedException handling.

Content CRUD (Hero, list sections), Settings, Preview, Publish, GraphQL, and Tests are **not yet implemented** (placeholder packages only).

### 1.2 Modified / created files (in scope)

| Area | Files |
|------|--------|
| **Entry** | `TobyResumeApplication.java` |
| **Config** | `SecurityConfig.java`, `MongoConfig.java`, `CorsConfig.java`, `AppSecurityProperties.java` |
| **Common** | `ContentState.java`, `BaseDocument.java`, `ApiResponse.java`, `ErrorBody.java`, `GlobalExceptionHandler.java`, `ResourceNotFoundException.java`, `UnauthorizedException.java`, `ForbiddenException.java`, `ValidationException.java`, `ValidLocaleKeys.java`, `LocaleKeysValidator.java`, `IdGenerator.java` |
| **Security** | `JwtTokenProvider.java`, `JwtAuthFilter.java`, `AuthPrincipal.java`, `OAuth2SuccessHandler.java`, `OAuth2FailureHandler.java`, `AuthController.java`, `HttpEnvelopeEntryPoint.java`, `HttpEnvelopeAccessDeniedHandler.java` |
| **Resources** | `application.yml`, `application-dev.yml` |
| **Infra** | `Dockerfile`, `docker-compose.yml` (from Phase A) |

### 1.3 Relevant design docs and constraints

- **api-design.md:** REST envelope (§2), error codes (§2.3), CORS (§1.2), OAuth2 flow (§3.1), JWT claims (§3.2), auth endpoints (§3.3).
- **database-design.md:** ContentState (§4.1), BaseDocument (§4.2), locale keys "en"/"vi" (§3), UUID for embedded items.
- **phase1-mvp.md:** Package structure (§3), security filter chain (§7.3), OAuth2 + JWT flow (§7.1–7.4), exception handling (§9), CORS (§10).
- **feature-api-conventions.md:** Error shape, HTTP status usage, CORS (incl. PATCH in requirements).
- **Constraints:** No auth for GraphQL; stateless sessions; 401/403 must return JSON envelope; allowed-admins can be email or GitHub id.

---

## 2. Design doc summary — what must be respected

### 2.1 api-design.md

| Decision / constraint | Requirement |
|------------------------|-------------|
| REST envelope | Success: `success`, `data`, `timestamp`; Error: `success`, `error` (code, message, details?), `timestamp`; no `data` on error. |
| Error codes | VALIDATION_ERROR, INVALID_LOCALE (400); UNAUTHORIZED (401); FORBIDDEN (403); RESOURCE_NOT_FOUND (404); PUBLISH_FAILED, INTERNAL_ERROR (500). |
| CORS | Origins, methods (GET, POST, PUT, DELETE, OPTIONS), headers (Content-Type, Authorization), credentials true. |
| OAuth2 | Redirect to Admin Panel with `?token=<jwt>` on success; `?error=forbidden` (or similar) when not allowed. |
| JWT | sub, name, provider, role, iat, exp; Bearer header; expiry configurable. |
| Auth endpoints | GET /api/v1/auth/me → email, name, role, provider; POST /api/v1/auth/logout → 200. |

### 2.2 database-design.md

| Decision / constraint | Requirement |
|------------------------|-------------|
| ContentState | Enum DRAFT, PUBLISHED; stored as string. |
| BaseDocument | _id, createdAt, updatedAt; auditing for timestamps. |
| Locale keys | Only "en" and "vi" at DTO layer. |
| Embedded IDs | UUID strings, not ObjectId. |

### 2.3 phase1-mvp.md

| Decision / constraint | Requirement |
|------------------------|-------------|
| Security chain | Permit /graphql, /actuator/health, /oauth2/**, /login/oauth2/**; require auth for /api/v1/**; stateless; JWT filter before UsernamePasswordAuthenticationFilter. |
| OAuth2 handlers | Success: check allowed-admins, issue JWT, redirect with token; Failure: redirect with error. |
| Exception handling | Map to envelope and HTTP status; AccessDeniedException → 403. |

### 2.4 feature-api-conventions.md

| Decision / constraint | Requirement |
|------------------------|-------------|
| Error body | code, message, optional details (field-level for 400). |
| CORS | Methods include PATCH (requirements); design §1.2 does not list PATCH. |

---

## 3. File-by-file comparison

### 3.1 Phase A (Foundation)

| File | Design intent | Implementation | Status |
|------|----------------|----------------|--------|
| `TobyResumeApplication.java` | Bootstrap Spring Boot app | @SpringBootApplication, main | ✅ |
| `application.yml` | Server, MongoDB, OAuth2 client (Google/GitHub), GraphQL, app.security, app.cors, management | Present; env placeholders | ✅ |
| `application-dev.yml` | Dev profile: GraphiQL, health details, CORS | Present | ✅ |
| `SecurityConfig` (Phase A) | Replaced by Phase C | Now Phase C chain (see below) | ✅ |
| `Dockerfile` / `docker-compose.yml` | Multi-stage build; MongoDB + app; healthcheck | Present | ✅ |

No deviations for Phase A.

---

### 3.2 Phase B (Common Layer)

Detailed comparison exists in **`verification-phase-b-common-layer.md`**. Summary:

| Component | Alignment | Notes |
|-----------|-----------|------|
| ContentState, BaseDocument, MongoConfig | ✅ | BaseDocument uses String id (design: ObjectId); acceptable with Spring Data. |
| ApiResponse, ErrorBody | ✅ | ErrorBody vs design “ErrorResponse” naming only. |
| GlobalExceptionHandler | ✅ | 400/401/403/404/500 + validation details; AccessDeniedException added in Phase C. INVALID_LOCALE not distinct (locale errors → VALIDATION_ERROR). |
| Custom exceptions | ✅ | Including ForbiddenException. |
| ValidLocaleKeys, LocaleKeysValidator | ✅ | Only "en", "vi". |
| CorsConfig | ✅ | PATCH not in design; optional to add per requirements. |
| IdGenerator | ✅ | UUID for embedded IDs. |

---

### 3.3 Phase C (Security & Auth)

#### 3.3.1 JwtTokenProvider

| Design | Implementation | Status |
|--------|----------------|--------|
| HS256 | Jwts.builder().signWith(key) with Keys.hmacShaKeyFor | ✅ |
| Claims: sub, name, provider, role, iat, exp | subject(), claim(name), claim(provider), claim(role), issuedAt(), expiration() | ✅ |
| Expiry configurable | @Value app.security.jwt.expiration-ms | ✅ |
| Parse and validate | parseSignedClaims; null on invalid/expired | ✅ |

**Risk:** `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))` (JJWT 0.12) requires secret length ≥ 256 bits (32 bytes). Default in yml is 32 chars; shorter production secret will throw at first token generation. **Recommendation:** Document minimum 32 characters or add startup validation.

---

#### 3.3.2 JwtAuthFilter

| Design | Implementation | Status |
|--------|----------------|--------|
| Read Authorization: Bearer &lt;token&gt; | Header "Authorization", strip "Bearer " | ✅ |
| Validate token | JwtTokenProvider.parseAndValidate | ✅ |
| Set SecurityContext | UsernamePasswordAuthenticationToken with AuthPrincipal | ✅ |
| Before UsernamePasswordAuthenticationFilter | addFilterBefore(jwtAuthFilter, ...) | ✅ |

When token is missing or invalid, filter does not set context; request then hits `/api/v1/**` unauthenticated and **HttpEnvelopeEntryPoint** returns 401 JSON. ✅

---

#### 3.3.3 OAuth2SuccessHandler

| Design | Implementation | Status |
|--------|----------------|--------|
| Check email/id in allowed-admins | isAllowed(): sub (email or github:id) or GitHub id in list | ✅ |
| If allowed: generate JWT, redirect with token | redirectUri?token=jwt | ✅ |
| If not allowed: redirect with error | redirectUri?error=forbidden | ✅ |
| Google: email as identity | sub = email (or sub); name from name | ✅ |
| GitHub: email or id | sub = email or "github:"+id; isAllowed checks id.toString() | ✅ |

**Design:** api-design §3.1 says redirect “with `?error=forbidden` (or similar)”. Implementation uses `error=forbidden` for not-allowed and `error=access_denied` in failure handler; also `unknown_provider`, `missing_identity`. ✅

**Edge case:** If `allowedAdmins` is empty, no one is allowed (handler redirects with error). Correct.

---

#### 3.3.4 OAuth2FailureHandler

| Design | Implementation | Status |
|--------|----------------|--------|
| Redirect to Admin Panel with error | redirectUri?error=access_denied | ✅ |

---

#### 3.3.5 SecurityConfig

| Design (phase1-mvp §7.3) | Implementation | Status |
|---------------------------|----------------|--------|
| Permit /graphql | requestMatchers("/graphql", "/graphql/**").permitAll() | ✅ |
| Permit /actuator/health | requestMatchers("/actuator/**").permitAll() | ✅ |
| Permit /oauth2/**, /login/oauth2/** | permitAll() for both | ✅ |
| Require auth for /api/v1/** | authenticated() | ✅ |
| anyRequest().denyAll() | present | ✅ |
| Session stateless | SessionCreationPolicy.STATELESS | ✅ |
| oauth2Login success/failure handlers | OAuth2SuccessHandler, OAuth2FailureHandler | ✅ |
| JWT filter before UsernamePasswordAuthenticationFilter | addFilterBefore(jwtAuthFilter, ...) | ✅ |
| 401/403 as JSON | HttpEnvelopeEntryPoint, HttpEnvelopeAccessDeniedHandler | ✅ (design: envelope for REST) |

---

#### 3.3.6 AuthController

| Design | Implementation | Status |
|--------|----------------|--------|
| GET /api/v1/auth/me | @GetMapping("/me"); path is /api/v1/auth (class) + /me | ✅ |
| Response: email, name, role, provider | AuthMeResponse record | ✅ |
| Wrapped in ApiResponse success | ApiResponse.success(data) | ✅ |
| POST /api/v1/auth/logout | @PostMapping("/logout"); 200 OK | ✅ |
| Logout: client discards token | No server-side blacklist (as design) | ✅ |

**Note:** /me is under /api/v1/** so it is protected; only valid JWT reaches the controller. Fallback 401 in controller when principal is not AuthPrincipal is defensive only.

---

#### 3.3.7 AppSecurityProperties

| Design | Implementation | Status |
|--------|----------------|--------|
| allowed-admins list | List&lt;String&gt; allowedAdmins; YAML list binds | ✅ |
| oauth2.redirect-uri | Oauth2.redirectUri (redirect-uri → redirectUri) | ✅ |

**Note:** JwtTokenProvider uses @Value for jwt.secret and expiration-ms; AppSecurityProperties also has Jwt inner class. Two sources of truth for JWT config; both read same yml. No conflict; optional refactor to use AppSecurityProperties.getJwt() in JwtTokenProvider for consistency.

---

#### 3.3.8 GlobalExceptionHandler — AccessDeniedException

| Design (phase1-mvp §9.1) | Implementation | Status |
|---------------------------|----------------|--------|
| AccessDeniedException → 403 | @ExceptionHandler(AccessDeniedException.class) → FORBIDDEN | ✅ |

---

## 4. Cross-cutting and security

### 4.1 Security

- **401/403:** Returned as JSON envelope (no HTML or redirect for API paths). ✅  
- **Secrets:** JWT secret and OAuth2 client secrets from config/env; no hardcoding. ✅  
- **CORS:** Driven by app.cors.*. ✅  
- **Stateless:** No server-side session; JWT only. ✅  
- **GraphQL:** No auth; public read-only as designed. ✅  

### 4.2 Requirements vs design

- **CORS PATCH:** feature-api-conventions mentions PATCH; api-design §1.2 does not. Implementation follows design (no PATCH). Add PATCH to allowed-methods if frontend or future API needs it.
- **Error envelope:** Requirements and design both require consistent error shape; implementation matches.

### 4.3 Logic gaps and edge cases

- **GitHub email:** With scope `user:email`, email may require an extra API call; Spring may not always populate it in attributes. If email is null, implementation uses `"github:"+id` as sub; allowed-admins can use GitHub numeric id. Document for deployers.
- **JWT secret length:** Document or validate ≥ 32 characters for HS256.
- **Redirect URI:** Must match Admin Panel callback URL exactly (scheme, host, port, path); no validation in code. Document in deployment/ops.

---

## 5. Missing pieces and tests

### 5.1 Not yet implemented (by design)

- Content CRUD (Hero, list sections), Settings, Preview, Publish, GraphQL schema/resolvers, Docker deploy polish, Testing & Docs phase. Placeholder packages only.

### 5.2 Missing tests

- **Phase B:** Unit tests for LocaleKeysValidator, IdGenerator; integration tests for GlobalExceptionHandler (and optionally CORS). See verification-phase-b-common-layer.md.
- **Phase C:** No unit tests for JwtTokenProvider (generate/parse/validate), OAuth2SuccessHandler (allowed/forbidden), or integration tests for AuthController (e.g. mock JWT or @WithMockUser) or 401/403 from SecurityConfig.

### 5.3 Documentation

- **Deployment/run:** How to set GOOGLE_*, GITHUB_*, ADMIN_EMAIL, JWT_SECRET, redirect URI; that JWT secret must be ≥ 32 chars.
- **Phase B verification:** Already documented in verification-phase-b-common-layer.md.

---

## 6. Summary and recommended next steps

### 6.1 Aligned with design and requirements

- **Phase A:** Bootstrap, config, Docker, Actuator, package layout.  
- **Phase B:** Envelope, error codes, validation details, CORS, locale validation, BaseDocument/auditing, ContentState, IdGenerator, custom exceptions, AccessDeniedException.  
- **Phase C:** OAuth2 Google/GitHub, JWT (HS256, correct claims), filter and SecurityContext, success/failure redirects, allowed-admins (email or GitHub id), SecurityConfig rules, 401/403 JSON, GET /auth/me, POST /auth/logout.

### 6.2 Minor deviations / choices

- BaseDocument uses **String id** (design mentions ObjectId); Spring Data mapping is acceptable.  
- **INVALID_LOCALE:** Locale validation errors return VALIDATION_ERROR; optional to map to INVALID_LOCALE.  
- **CORS:** PATCH not included; add if requirements are adopted.  
- **OAuth2 error param:** Design says “?error=forbidden or similar”; implementation uses `forbidden` for not-allowed and `access_denied` for failure handler; acceptable.

### 6.3 Recommended next steps

**High priority**

1. **Document JWT secret:** In deployment/README or .env.example, state that `JWT_SECRET` must be at least 32 characters (or add startup check in JwtTokenProvider).
2. **Phase C tests:** Add unit tests for JwtTokenProvider and OAuth2SuccessHandler (allowed/forbidden); add integration test for GET /api/v1/auth/me with mock JWT and for 401 without token.

**Medium priority**

3. **Phase B tests:** Per verification-phase-b-common-layer.md: unit tests for LocaleKeysValidator and IdGenerator; integration test for GlobalExceptionHandler.
4. **Optional:** Map @ValidLocaleKeys violations to error code INVALID_LOCALE.
5. **Optional:** Add PATCH to CORS allowed-methods if required by frontend or API.

**Lower priority**

6. **Consistency:** Consider injecting AppSecurityProperties into JwtTokenProvider and using getJwt().getSecret() / getExpirationMs() instead of @Value.
7. **Deployment doc:** Document OAuth2 redirect URI, allowed-admins (email vs GitHub id), and GitHub email scope behavior.

### 6.4 Verification checklist

- [x] Phase A, B, C implemented per plan.  
- [x] REST envelope and error codes match api-design §2, §2.3.  
- [x] CORS and security chain match design.  
- [x] OAuth2 flow and JWT claims match design.  
- [x] Auth endpoints and 401/403 JSON match design.  
- [ ] Tests for Phase B and Phase C (recommended).  
- [ ] JWT secret length and OAuth2/redirect docs (recommended).

**Overall:** Implementation is **aligned** with design and requirements for Phases A, B, and C. Remaining work is tests, documentation (JWT secret, OAuth2/redirect), and optional refinements (INVALID_LOCALE, PATCH, single source for JWT config).

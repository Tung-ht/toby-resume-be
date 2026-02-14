# Implementation Verification — Phase F (Settings)

**Feature:** Phase F — Site settings CRUD and bootstrap (M6)  
**Scope:** Settings module only  
**Design refs:** `docs/ai/design/database-design.md` §5.8, §8.4; `docs/ai/design/api-design.md` §6; `docs/ai/design/phase1-mvp.md`; `docs/ai/requirements/feature-core-cms.md` (Global / Settings)

---

## 1. Context

| Item | Description |
|------|-------------|
| **Feature/branch** | Phase F (Settings): GET/PUT `/api/v1/settings`, singleton document, bootstrap on first access |
| **Modified/added files** | `settings/model/SiteSettings.java`, `settings/dto/SiteSettingsRequest.java`, `settings/dto/SiteSettingsResponse.java`, `settings/SettingsMapper.java`, `settings/SettingsRepository.java`, `settings/SettingsService.java`, `settings/SettingsController.java` |
| **Design docs** | database-design (§5.8, §8.4), api-design (§6), phase1-mvp (package layout), feature-core-cms (Global / Settings) |
| **Constraints** | Phase 1: supportedLocales fixed `["en","vi"]`; JWT required for `/api/v1/**`; unified REST envelope |

---

## 2. Design Summary (Key Decisions & Constraints)

### database-design.md

- **§5.8 `site_settings`:** Singleton (exactly 1 document). Fields: `supportedLocales` (String[], required, fixed `["en","vi"]`), `defaultLocale` (required, one of supportedLocales), `pdfSectionVisibility` (Map<String, Boolean>, keys: hero, experiences, projects, education, skills, certifications, socialLinks). Extends base with `_id`, `createdAt`, `updatedAt`. Bootstrap: insert default if no document exists.
- **§8.4 Initial state:** `site_settings` — 1 document, bootstrapped with defaults (design text says “on first startup”; implementation uses “on first access” — see deviations).
- **§4.2 BaseDocument:** id, createdAt, updatedAt; auditing via MongoConfig.

### api-design.md

- **§6 Settings:** GET and PUT `/api/v1/settings`; JWT required. PUT body: supportedLocales, defaultLocale, pdfSectionVisibility. Response (200): envelope with `data` = saved settings object.
- **§2 Envelope:** success, data, timestamp on success; success, error (code, message, details), timestamp on error. Validation → 400, VALIDATION_ERROR.

### phase1-mvp.md

- Package layout: `settings/model/SiteSettings.java`, `settings/dto/*`, `SettingsRepository`, `SettingsService`, `SettingsController` — matches.

### feature-core-cms.md

- Global / Settings: supported locales `["en","vi"]`, default locale; PDF section visibility for Hero, Experience, Projects, Education, Skills, Certifications, Social Links. No theme/layout in backend — aligned.

---

## 3. File-by-File Comparison

### settings/model/SiteSettings.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| Collection | `site_settings` | `@Document(collection = "site_settings")` | Match |
| Base type | Extends base (id, createdAt, updatedAt) | Extends `BaseDocument` | Match |
| supportedLocales | String[], required | String[] | Match |
| defaultLocale | String, required | String | Match |
| pdfSectionVisibility | Map<String, Boolean>, required, 7 keys | Map<String, Boolean> | Match |
| _id type | ObjectId in doc | String id (Spring Data convention) | Match (project convention) |

**Deviations:** None.  
**Notes:** BaseDocument uses `String id`; design shows ObjectId — consistent with rest of project (e.g. Hero).

---

### settings/dto/SiteSettingsRequest.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| supportedLocales | String[], fixed ["en","vi"] | @NotNull, @NotEmpty | Match (exact set enforced in service) |
| defaultLocale | String, in supportedLocales | @NotNull | Match (in-set enforced in service) |
| pdfSectionVisibility | Map, keys fixed | @NotNull | Match (keys enforced in service) |

**Deviations:** None. Phase 1 fixed values are enforced in `SettingsService.validateRequest()`.  
**Suggestions:** Optional: add `@Size(2)` on supportedLocales and document that order may be normalized if needed.

---

### settings/dto/SiteSettingsResponse.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| Data shape | Saved settings object | id, supportedLocales, defaultLocale, pdfSectionVisibility, createdAt, updatedAt | Match |
| Timestamps | Design implies audit fields | Instant createdAt, updatedAt | Match |

**Deviations:** None.

---

### settings/SettingsMapper.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| Entity → response | Required | `toResponse(SiteSettings)` | Match |
| Request → entity | Implied for PUT | Done via setters in service | Acceptable (no mapper for update) |

**Deviations:** None. Explicit setters in service are a deliberate simplification.

---

### settings/SettingsRepository.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| Singleton access | One document per app | `findSingleton()` = findAll().isEmpty() ? null : get(0) | Match |
| Persistence | MongoRepository | MongoRepository<SiteSettings, String> | Match |

**Deviations:** None.  
**Edge case:** If more than one document were ever present (e.g. manual DB edit), `findSingleton()` returns the first; no unique index on the collection. Design says “exactly 1 document” — consider adding a unique constraint or document the “single doc” invariant.

---

### settings/SettingsService.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| getOrCreate | If no doc, insert default | findSingleton(); if null, create default + save | Match |
| Default supportedLocales | ["en", "vi"] | DEFAULT_SUPPORTED_LOCALES | Match |
| Default defaultLocale | "en" | DEFAULT_LOCALE | Match |
| Default pdfSectionVisibility | All true except socialLinks false | defaultPdfSectionVisibility() | Match |
| PUT validation | defaultLocale in supportedLocales; pdf keys fixed | validateRequest() | Match |
| supportedLocales Phase 1 | Fixed ["en","vi"] | Set.equals(Set.of("en","vi")) | Match |
| Update flow | Save and return | Load or create, set fields, save, return response | Match |

**Deviations:**

1. **Bootstrap timing:** Design §5.8 says “on first startup”; implementation bootstraps on **first access** (first GET or first PUT). Rationale: avoids blocking startup or requiring a dedicated init bean; first request creates the document. Acceptable; consider documenting in design or this doc.

**Logic / edge cases:**

- **Null request fields:** Jakarta validation ensures non-null for all three fields before service runs; validateRequest still has null checks for safety — good.
- **Empty supportedLocales:** Rejected by @NotEmpty.
- **Duplicate keys in pdfSectionVisibility:** Map allows only one value per key; extra keys fail the “exactly these keys” check — good. Missing keys also fail — good.

**Suggestions:** For ValidationException from service, consider passing `List<FieldErrorDetail>` (e.g. field `"defaultLocale"`, message “must be one of supportedLocales”) so clients get structured details like @Valid failures.

---

### settings/SettingsController.java

| Aspect | Design | Implementation | Status |
|--------|--------|----------------|--------|
| GET /api/v1/settings | JWT, return settings | GET, getOrCreate(), ApiResponse.success(data) | Match |
| PUT /api/v1/settings | JWT, body, return saved | PUT, @Valid request, update(), ApiResponse.success(data) | Match |
| Auth | JWT required | SecurityConfig: /api/v1/** authenticated | Match |
| Envelope | success, data, timestamp | ApiResponse.success(data) | Match |

**Deviations:** None.  
**Security:** No additional checks beyond JWT; only allowed admins can reach the controller (OAuth2 + allowed-admins). No sensitive data exposure; settings are admin-only. Good.

---

## 4. Requirements Alignment (feature-core-cms.md)

- **Supported locales ["en", "vi"], default locale:** Implemented and validated.
- **PDF section visibility (subset of sections):** Implemented with exact keys; all seven sections represented; values are booleans. Requirement satisfied.
- **No theme/layout in backend:** No extra fields; aligned.

---

## 5. Gaps, Risks, and Recommendations

### Confirmed alignment

- Schema, API contract, package layout, and auth match the design and requirements.
- Bootstrap defaults match the example in database-design.
- Validation (Phase 1 supportedLocales, defaultLocale, pdfSectionVisibility keys) is enforced.
- Error handling uses ValidationException → 400 with VALIDATION_ERROR.

### Minor deviations (accepted)

- **Bootstrap:** “First access” instead of “first startup” — documented above; no change required unless product explicitly requires startup-time bootstrap.

### Suggested improvements

| Priority | Item | Action |
|----------|------|--------|
| Medium | **Structured validation details** | Have SettingsService pass `List<FieldErrorDetail>` into ValidationException for PUT (e.g. defaultLocale, pdfSectionVisibility) so 400 responses match MethodArgumentNotValidException shape. |
| Low | **Singleton guarantee** | Document that only one document is expected, or add an application-level unique constraint / migration that ensures a single document. |
| Low | **Tests** | Add unit tests for SettingsService (getOrCreate when empty vs existing, update with valid/invalid payloads) and integration test for SettingsController (GET/PUT, 400 on invalid body, 401 without JWT). |

### Missing tests

- No unit tests for `SettingsService`.
- No integration tests for `SettingsController`.
- Planning doc (Phase K) defers broader test coverage; Phase F-specific tests are recommended.

### Documentation updates

- Planning doc already updated (Phase F complete).
- Optional: add one sentence in database-design §5.8 or §8.4 that bootstrap may occur “on first access” if the project standardizes on that.

---

## 6. Summary and Next Steps

**Verdict:** Phase F implementation **matches** the design and requirements. No blocking deviations. Bootstrap timing is “first access” vs “first startup” and is acceptable.

**Recommended next steps:**

1. **Optional:** Add field-level details for settings validation in `ValidationException` for PUT.
2. **Recommended:** Add unit tests for `SettingsService` and at least one integration test for `SettingsController` (GET, PUT success, PUT validation error, 401 without JWT).
3. **Optional:** Document or enforce single-document invariant for `site_settings` (e.g. in docs or via constraint).
4. Proceed with Phase D (Hero CRUD) or Phase G/H when ready; Settings is ready for use by Preview/Publish and GraphQL (e.g. defaultLocale, supportedLocales).

---

*Verification date: 2026-02-12. Phase F implementation review.*

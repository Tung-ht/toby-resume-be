# Phase D (Hero CRUD) — Implementation vs Design Review

**Date:** 2026-02-12  
**Scope:** Phase D implementation (Hero singleton section) compared to `docs/ai/design/` and `docs/ai/requirements/`.

---

## 1. Context

| Item | Value |
|------|--------|
| **Feature** | Phase D — Content CRUD Hero (singleton section, DRAFT-only) |
| **Modified/added files** | `content/hero/` (model, dto, mapper, repository, service, controller), `common/validation/MapValueMaxLength*`, `common/dto/ApiResponse.java` |
| **Design docs** | `api-design.md` §4.1, `database-design.md` §5.1, §7, §8.1, §9; `phase1-mvp.md` (Hero package structure, flow) |
| **Requirements** | `feature-core-cms.md` (Hero schema, CRUD, draft/publish), `feature-api-conventions.md` (envelope, 400/401, validation details) |
| **Constraints** | JWT required for `/api/v1/**`; no GraphQL Hero resolver in Phase D (Phase I); backward compatibility N/A (new feature) |

---

## 2. Design Summary (Key Decisions & Constraints)

### 2.1 API Design (§4.1, §2)

- **Endpoints:** `GET /api/v1/hero` (draft or `data: null`), `PUT /api/v1/hero` (create/update draft, idempotent, full replace).
- **Auth:** All content endpoints require JWT; 401 for missing/invalid token.
- **Envelope:** Success: `{ success: true, data, timestamp }`; error: `{ success: false, error: { code, message, details? }, timestamp }`.
- **Validation errors:** 400 with `error.code === "VALIDATION_ERROR"` and `error.details` array of `{ field, message }` (e.g. `field: "hero.tagline"`).
- **PUT body:** `tagline`, `bio`, `fullName`, `title` (each `Map<String,String>`, keys en/vi, max 500/2000/200/200 chars per value); `profilePhotoMediaId` (string or null).
- **Response (200):** `data` = saved hero including `updatedAt`.

### 2.2 Database Design (§5.1, §7, §8.1, §9)

- **Collection:** `hero`; max 2 documents (one DRAFT, one PUBLISHED); unique index on `contentState`.
- **Fields:** `contentState`, `tagline`, `bio`, `fullName`, `title` (all `Map<String,String>` with `@ValidLocaleKeys`), `profilePhotoMediaId` (String, nullable), `createdAt`, `updatedAt`.
- **Lifecycle:** First PUT creates DRAFT; subsequent PUTs update same DRAFT. Publish (Phase H) copies DRAFT → PUBLISHED.
- **Validation:** Max 500 (tagline), 2000 (bio), 200 (fullName, title) per locale value; locale keys only "en", "vi".

### 2.3 Phase 1 MVP (package structure, flow)

- **Package:** `content/hero/` with `model/Hero.java`, `dto/HeroRequest.java`, `dto/HeroResponse.java`, `HeroMapper.java`, `HeroRepository.java`, `HeroService.java`, `HeroController.java`. No `HeroGraphQL.java` until Phase I.

---

## 3. File-by-File Comparison

### 3.1 `content/hero/model/Hero.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| `@Document(collection = "hero")` | Present | ✅ |
| Extends BaseDocument (_id, createdAt, updatedAt) | Extends BaseDocument | ✅ |
| `@Indexed(unique = true) ContentState contentState` | Present | ✅ |
| `@ValidLocaleKeys` on tagline, bio, fullName, title | Present | ✅ |
| `Map<String,String>` for localized fields | Yes | ✅ |
| `String profilePhotoMediaId` | Yes | ✅ |

**Note:** BaseDocument uses `String id`; design doc shows `_id` as ObjectId. Spring Data MongoDB accepts both; String is a valid implementation choice for REST/JSON. No change required.

---

### 3.2 `content/hero/dto/HeroRequest.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| tagline: Map, keys en/vi, max 500 per value | `@ValidLocaleKeys` + `@MapValueMaxLength(500)` | ✅ |
| bio: max 2000 per value | `@MapValueMaxLength(2000)` | ✅ |
| fullName, title: max 200 per value | `@MapValueMaxLength(200)` | ✅ |
| profilePhotoMediaId: String or null | `@Size(max = 500)` (optional) | ✅ |

**Deviation:** Design does not specify max length for `profilePhotoMediaId`. Implementation uses 500 as a safe upper bound for a reference (e.g. UUID or path). **Acceptable;** document in API design if desired.

---

### 3.3 `content/hero/dto/HeroResponse.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| Same fields as request + updatedAt | tagline, bio, fullName, title, profilePhotoMediaId, updatedAt | ✅ |
| updatedAt in response | `Instant updatedAt` | ✅ |

**Aligned.**

---

### 3.4 `content/hero/HeroMapper.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| request → entity (DRAFT) | `requestToEntity` with `contentState = DRAFT`, id/createdAt/updatedAt ignored | ✅ |
| update entity from request | `updateEntityFromRequest` | ✅ |
| entity → response | `entityToResponse` | ✅ |

**Aligned.**

---

### 3.5 `content/hero/HeroRepository.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| `findByContentState(ContentState)` | Present | ✅ |
| MongoRepository<Hero, ?> | `MongoRepository<Hero, String>` (id type matches BaseDocument) | ✅ |

**Aligned.**

---

### 3.6 `content/hero/HeroService.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| getDraft() → null if absent | Returns null when no DRAFT | ✅ |
| upsertDraft(request): create or update single DRAFT | Find DRAFT; if null create from request, else update from request; save; return response | ✅ |
| Idempotent, full replace | Full replace via updateEntityFromRequest | ✅ |

**Aligned.**

---

### 3.7 `content/hero/HeroController.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| GET `/api/v1/hero` | `@GetMapping("/hero")` on `@RequestMapping("/api/v1")` | ✅ |
| PUT `/api/v1/hero` | `@PutMapping("/hero")` | ✅ |
| Return ApiResponse envelope | `ResponseEntity.ok(ApiResponse.success(data))` | ✅ |
| GET: 200 with data or data null | ApiResponse.success(data) with data possibly null | ✅ |
| PUT: @Valid request body | `@Valid @RequestBody HeroRequest request` | ✅ |
| JWT required | Enforced by SecurityConfig for `/api/v1/**` | ✅ |

**Aligned.** Success response always includes `data` (null when no draft) after ApiResponse change; matches design “200 with data: null”.

---

### 3.8 `common/dto/ApiResponse.java`

| Design | Implementation | Status |
|--------|----------------|--------|
| Success: success, data, timestamp | Present; `data` always serialized (`@JsonInclude(ALWAYS)` on data field) so GET hero returns `"data": null` when no draft | ✅ |

**Aligned.** Ensures stable envelope for clients.

---

### 3.9 `common/validation/MapValueMaxLength` & `MapValueMaxLengthValidator`

| Design | Implementation | Status |
|--------|----------------|--------|
| Max length per locale value (database-design §9) | Annotation + validator over Map values | ✅ |
| Null/empty map allowed | Validator returns true for null or empty | ✅ |

**Aligned.** Reusable for other localized sections.

---

### 3.10 Error Handling & Validation Details

| Design | Implementation | Status |
|--------|----------------|--------|
| 400 for validation; code VALIDATION_ERROR; details with field + message | GlobalExceptionHandler handles MethodArgumentNotValidException → 400, VALIDATION_ERROR, details | ✅ |
| Example field path "hero.tagline" | Spring BindingResult objectName for HeroRequest is "heroRequest" → path "heroRequest.tagline" | ⚠️ Minor |

**Deviation:** api-design example shows `"field": "hero.tagline"`. Implementation produces `"heroRequest.tagline"`. Functionally equivalent; "hero" is friendlier for clients. **Recommendation:** Optional: use `@RequestBody("hero")` or a custom method in GlobalExceptionHandler to normalize to "hero.*" for HeroRequest so docs and clients match.

---

### 3.11 Security

| Design | Implementation | Status |
|--------|----------------|--------|
| /api/v1/* requires JWT | SecurityConfig: `.requestMatchers("/api/v1/**").authenticated()` | ✅ |
| 401 without auth | Integration test `getHero_unauthorized_withoutAuth` | ✅ |

**Aligned.**

---

### 3.12 Tests

| Design / Plan | Implementation | Status |
|---------------|----------------|--------|
| Unit: HeroService (get null, get with data, upsert create, upsert update) | HeroServiceTest: 4 tests | ✅ |
| Integration: HeroController (GET/PUT, auth, validation 400) | HeroControllerIntegrationTest: 6 tests, Testcontainers MongoDB, @Order for stability | ✅ |
| Edge: no draft → GET returns 200 + null | Test `getHero_returnsNullData_whenNoDraft` | ✅ |
| Validation: e.g. tagline > 500 → 400 | Test `putHero_returns400_whenValidationFails` | ✅ |

**Aligned.** No missing test areas identified for Phase D scope.

---

## 4. Gaps, Edge Cases, Security

- **Logic gaps:** None. Empty request body is valid (all fields optional); upsert creates document with null/empty maps as designed.
- **Edge cases:** Empty DB, no DRAFT → GET returns 200 + `data: null` (tested). PUT with empty body creates DRAFT with null fields (acceptable).
- **Security:** No injection risk; validation and type-safe DTOs. JWT required; no extra authorization beyond “authenticated” (allowed-admins enforced at OAuth2 login). No sensitive data in response.
- **INVALID_LOCALE:** Design calls out 400 with code INVALID_LOCALE for invalid locale keys. `@ValidLocaleKeys` triggers Bean Validation; it is handled as VALIDATION_ERROR with message from validator. Design does not require a separate error code for locale; current behavior is acceptable. Optional: map locale-specific message to INVALID_LOCALE in handler for consistency with api-design table.

---

## 5. Simplifications / Refactors

- **None required.** Structure is clear: controller → service → repository; mapper keeps DTO/entity conversion in one place. Optional: add a one-line Javadoc to HeroController that “GET returns 200 with data or data null” to mirror api-design wording.

---

## 6. Missing Tests / Documentation

- **Tests:** Phase D coverage is sufficient. Optional: one integration test that PUT with invalid locale key returns 400 (e.g. `tagline: { "fr": "x" }`) to lock in `@ValidLocaleKeys` behavior.
- **Documentation:** Implementation plan and README updated. Optional: in api-design §4.1, add a short note that validation error `field` may be request-scoped (e.g. `heroRequest.tagline`) unless normalized.

---

## 7. Summary & Recommended Next Steps

### Findings Summary

| Category | Count |
|----------|--------|
| Full alignment | 10/10 component areas |
| Minor deviations | 2 (validation field path; profilePhotoMediaId max length) |
| Logic/security issues | 0 |
| Required refactors | 0 |

Phase D implementation matches design and requirements. Remaining items are optional polish.

### Recommended Next Steps (Priority)

1. **Optional — Validation field path:** If you want error `field` to match api-design exactly, normalize Hero request errors to `"hero.*"` in GlobalExceptionHandler (or use `@RequestBody("hero")` and ensure objectName is "hero"). Low effort.
2. **Optional — Test invalid locale:** Add one integration test: PUT hero with `tagline: { "fr": "x" }` → 400 and (if applicable) error details mentioning locale. Low effort.
3. **Optional — Docs:** In api-design §2.2 or §4.1, note that `profilePhotoMediaId` may be constrained (e.g. max 500 chars) and that validation `field` names may be request-scoped. Low effort.
4. **Proceed to Phase E** as planned; no Phase D blockers.

---

*Review completed per project check-implementation / design-comparison workflow.*

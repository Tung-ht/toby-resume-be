# Phase E (Content CRUD — List Sections) — Implementation vs Design Review

**Date:** 2026-02-12  
**Scope:** All Phase E list sections (Experience, Projects, Education, Certifications, Social Links, Skills) compared to `docs/ai/design/` and `docs/ai/requirements/`.

---

## 1. Context

| Item | Value |
|------|--------|
| **Feature** | Phase E — Content CRUD for all list sections (M3); DRAFT-only REST; 6 endpoints per section (list, get, add, update, delete, reorder); Skills uses category CRUD + reorder. |
| **Modified/added files** | `content/experience/`, `content/project/`, `content/education/`, `content/certification/`, `content/sociallink/`, `content/skill/` (models, DTOs, mappers, repositories, services, controllers); `common/dto/ReorderRequest.java`; `common/model/Link.java`; `common/validation/ValidBulletPoints.java`, `BulletPointsValidator.java`. |
| **Design docs** | `api-design.md` §4.2–4.7; `database-design.md` §4.3 (Link), §5.2–5.7 (collections), §7 (indexes), §8.1 (lifecycle), §9 (validation). |
| **Requirements** | `feature-core-cms.md` (content schema, CRUD, locales en/vi); `feature-api-conventions.md` (envelope, 400/401, validation details). |
| **Constraints** | JWT required for `/api/v1/**`; no GraphQL in Phase E; backward compatibility N/A (new feature). |

---

## 2. Design Summary (Key Decisions & Constraints)

### 2.1 API Design (§4.2–4.7)

- **Common pattern (Experience, Projects, Education, Certifications, Social Links):**  
  GET list, GET `/{itemId}`, POST, PUT `/{itemId}`, DELETE `/{itemId}`, PUT `/reorder` with body `{ "orderedIds": ["id1", "id2", ...] }`. All DRAFT-only; JWT required.
- **Skills:** GET list, POST (add category), PUT `/{categoryId}` (update category), DELETE `/{categoryId}`, PUT `/reorder` (orderedIds = categoryIds). No GET-by-categoryId in design table (implementation adds it).
- **Reorder response:** Design allows “updated list” or `{ "reordered": true }`; implementation returns `ApiResponse.success(null)` — acceptable.
- **Validation:** Per-section field rules (max lengths, required, locale keys en/vi, date formats YYYY-MM). 400 with `VALIDATION_ERROR` and `details` for invalid input; 404 for missing item/category.

### 2.2 Database Design (§5.2–5.7, §7, §8.1, §9)

- **Collections:** `work_experiences`, `projects`, `education`, `certifications`, `social_links`, `skills`. Each has unique index on `contentState`; max one DRAFT and one PUBLISHED document.
- **Lifecycle:** First write creates DRAFT document (lazy); reorder updates `order` by position in `orderedIds`.
- **Link (Projects):** label (max 100), url (valid URL). Embedded in ProjectItem.
- **Validation matrix:** Experience (company/role 200, bulletPoints 10 per locale/500 each, startDate/endDate YYYY-MM); Projects (title 200, description 3000, links 10, mediaIds 10); Education (institution/degree/field 200, details 1000, startDate/endDate YYYY-MM); Certifications (title/issuer 200, description 500, date/url optional); Social Links (platform 50, url valid); Skills (category name 100, items 50 per category, item name 100).

### 2.3 Requirements (feature-core-cms)

- All sections support en/vi only; list sections with order; Skills as categories + items. Aligned with implementation.

---

## 3. File-by-File Comparison

### 3.1 Experience

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `work_experiences` | `@Document(collection = "work_experiences")` | ✅ |
| Paths `/api/v1/experiences`, `/{itemId}`, `/reorder` | Controller mappings match | ✅ |
| company, role required ≥1 locale, max 200 | `@NotNull` `@Size(min=1)` `@ValidLocaleKeys` `@MapValueMaxLength(200)` | ✅ |
| startDate required YYYY-MM; endDate optional | `@NotBlank` `@Pattern(\\d{4}-\\d{2})`; endDate `@Pattern` only | ✅ |
| bulletPoints max 10 per locale, 500 per item | `@ValidBulletPoints` (default 10, 500) | ✅ |
| techUsed, order optional | No extra validation | ✅ |
| Reorder body orderedIds | `ReorderRequest.orderedIds` `@NotNull` | ✅ |
| 404 for missing item | `ResourceNotFoundException` in service; handler → RESOURCE_NOT_FOUND | ✅ |

**Deviation:** None.

---

### 3.2 Projects

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `projects` | `@Document(collection = "projects")` | ✅ |
| Paths `/api/v1/projects`, `/{itemId}`, `/reorder` | Match | ✅ |
| title required, max 200 per locale | `@NotNull` `@Size(min=1)` `@ValidLocaleKeys` `@MapValueMaxLength(200)` | ✅ |
| description max 3000; techStack; links max 10; mediaIds max 10 | DTO + `@Size(max=10)` on links/mediaIds; LinkDto for links | ✅ |
| visible boolean default true | `Boolean visible = true` in request | ✅ |
| Link: label max 100, url valid | `LinkDto`: label `@NotBlank` `@Size(100)`; url `@Pattern(https?\|ftp://...)` | ⚠️ Minor |

**Deviation:** Link `url` pattern allows only `http(s)` and `ftp`. Design says “Valid URL format”; `mailto:` is not allowed in Projects links. Acceptable if project links are only demo/repo URLs; document or extend pattern if contact links are needed.

---

### 3.3 Education

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `education` | `@Document(collection = "education")` | ✅ |
| Paths `/api/v1/education`, `/{itemId}`, `/reorder` | Match | ✅ |
| institution, degree required max 200; field optional max 200 | `@NotBlank` `@Size(200)` on institution/degree; `@Size(200)` on field | ✅ |
| startDate/endDate YYYY-MM | `@NotBlank` `@Pattern` startDate; `@Pattern` endDate (optional) | ✅ |
| details Map en/vi max 1000 | `@ValidLocaleKeys` `@MapValueMaxLength(1000)` | ✅ |

**Deviation:** None.

---

### 3.4 Certifications

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `certifications` | `@Document(collection = "certifications")` | ✅ |
| Paths `/api/v1/certifications`, `/{itemId}`, `/reorder` | Match | ✅ |
| title, issuer max 200 | `@NotBlank` `@Size(200)` | ✅ |
| date string (optional); url optional; description optional max 500 | date `@Pattern(YYYY-MM or YYYY-MM-DD)` optional; url `@Size(2048)`; description `@ValidLocaleKeys` `@MapValueMaxLength(500)` | ✅ |

**Deviation:** None. Date optional (no `@NotBlank`); pattern allows empty string for optional date.

---

### 3.5 Social Links

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `social_links` | `@Document(collection = "social_links")` | ✅ |
| Paths `/api/v1/social-links`, `/{itemId}`, `/reorder` | Match (kebab-case) | ✅ |
| platform max 50; url valid URL; icon optional | `@NotBlank` `@Size(50)` platform; url `@Pattern(https?|ftp|mailto)`; icon `@Size(200)` | ✅ |

**Deviation:** None. Icon max length not in design; 200 is a safe cap.

---

### 3.6 Skills

| Design | Implementation | Status |
|--------|----------------|--------|
| Collection `skills` | `@Document(collection = "skills")` | ✅ |
| GET list, POST, PUT `/{categoryId}`, DELETE `/{categoryId}`, PUT reorder | All implemented | ✅ |
| Category name Map max 100; items max 50; item name max 100, level optional | `SkillCategoryRequest`: name `@ValidLocaleKeys` `@MapValueMaxLength(100)`; items `@Size(max=50)`; `SkillItemRequest` name `@NotBlank` `@Size(100)`, level `@Size(50)` | ✅ |
| orderedIds = categoryIds | ReorderRequest; service validates exact ID set | ✅ |

**Extension:** Implementation adds **GET `/api/v1/skills/{categoryId}`** (get one category). Not in api-design table; consistent with other list sections and useful for clients. **Recommendation:** Document in api-design §4.5.

---

### 3.7 Shared Components

| Component | Design | Implementation | Status |
|-----------|--------|----------------|--------|
| ReorderRequest | Body `{ "orderedIds": ["id1", ...] }` | `ReorderRequest.orderedIds` `@NotNull` | ✅ |
| Empty orderedIds | — | Service returns early; no error | ✅ |
| orderedIds mismatch | — | `ValidationException` → 400 VALIDATION_ERROR | ✅ |
| Link (embedded) | database-design §4.3 | `common/model/Link.java`; ProjectItem uses it | ✅ |

---

### 3.8 Security & Envelope

| Design | Implementation | Status |
|--------|----------------|--------|
| All list endpoints require JWT | SecurityConfig `.requestMatchers("/api/v1/**").authenticated()` | ✅ |
| Success envelope | `ApiResponse.success(data)`; list endpoints return `List<...Response>`; reorder/delete return `success(null)` | ✅ |
| 401 without auth | Integration tests (Experience, Project) verify 401 | ✅ |

---

## 4. Logic, Edge Cases, Security

- **Draft creation:** All sections use getOrCreateDraft() and mutable list (e.g. `new ArrayList<>()`); first add creates DRAFT document. Matches design §8.1.
- **Reorder semantics:** orderedIds must equal current item/category IDs exactly; otherwise 400. Design does not specify partial reorder; current rule is clear and safe.
- **Empty list:** GET list returns `[]` when no DRAFT or empty items; envelope `data` is empty array. Aligned with design.
- **Security:** No new auth rules; validation and DTOs limit injection; no sensitive data in responses.
- **Skills items in category:** Service sets items from request via mapper; max 50 enforced in DTO. Null items in request result in empty list in category (service sets empty list when null). Acceptable.

---

## 5. Gaps & Missing Pieces

- **Tests:** Experience and Project have full unit (service) and integration (controller) tests. Education, Certification, SocialLink, and Skills have **no dedicated test classes**. E.8 required “unit tests for one list service; integration tests for one list controller” — satisfied. **Recommendation:** Add at least one integration test per remaining section (e.g. list empty, add, get 404) for regression safety and consistency.
- **GET /skills/{categoryId}:** Implemented but not listed in api-design §4.5. **Recommendation:** Add to api-design table and response shape.
- **Link url (Projects):** Pattern excludes `mailto:`. **Recommendation:** If product needs mailto links in project links, extend `LinkDto` url pattern or document that only http(s)/ftp are supported.

---

## 6. Simplifications / Refactors

- **No required refactors.** List-section code is consistent: getOrCreateDraft, list/get/add/update/delete/reorder, ValidationException for reorder mismatch, ResourceNotFoundException for missing item/category. Optional: extract a small “list section” shared doc or checklist for future sections.
- **Readability:** Some services use compact style (single-line conditionals). Acceptable; consider a line break before `throw` or in long streams if team prefers.

---

## 7. Documentation Updates

- **api-design.md:** Add GET `/api/v1/skills/{categoryId}` to §4.5 table; optionally note reorder response as `data: null` or `{ reordered: true }`.
- **database-design.md:** No change required; implementation matches.
- **implementation-plan-phase1-mvp.md:** Already updated; Phase E marked complete.

---

## 8. Summary & Recommended Next Steps

### Findings Summary

| Category | Count |
|----------|--------|
| Full alignment | All 6 sections + shared components |
| Minor deviations | 1 (Link url pattern in Projects — no mailto) |
| Extensions | 1 (GET /skills/{categoryId}) |
| Logic/security issues | 0 |
| Missing tests | 4 sections without dedicated tests (pattern covered by Experience/Project) |

Phase E implementation matches design and requirements. Remaining items are optional polish and documentation.

### Recommended Next Steps (Priority)

1. **Optional — api-design:** Add GET `/api/v1/skills/{categoryId}` to §4.5 and, if desired, document reorder response shape. **Effort:** S.
2. **Optional — Tests:** Add minimal integration tests for Education, Certifications, Social Links, Skills (e.g. list empty, add one, get 404). **Effort:** M.
3. **Optional — Link url:** If product requires `mailto:` in project links, extend `LinkDto` url pattern to include `mailto:`. **Effort:** S.
4. **Proceed to Phase G (Preview)** and Phase H (Publish); no Phase E blockers.

---

*Review completed per project design-comparison workflow.*

# Phase G (Preview API) — Design vs Implementation Review

**Review date:** 2026-02-12  
**Scope:** Phase G implementation (Preview API)  
**Design refs:** `docs/ai/design/api-design.md` §5.1, `docs/ai/design/phase1-mvp.md` §6.2  
**Requirements ref:** `docs/ai/requirements/feature-core-cms.md` (Preview API)

---

## 1. Scope and Context

### 1.1 Feature / branch description

**Phase G (Preview API):** Single authenticated REST endpoint that returns all DRAFT sections (hero, experiences, projects, education, skills, certifications, socialLinks) in one payload so the admin panel can render a full-page preview without multiple CRUD calls. Optional `?locale=en` or `?locale=vi` returns single-locale values per field (GraphQL-friendly shape for frontend reuse).

### 1.2 Modified / added files (Phase G)

| File | Purpose |
|------|---------|
| `src/main/java/.../preview/dto/PreviewResponse.java` | Root response DTO (hero + 6 section lists) |
| `src/main/java/.../preview/PreviewService.java` | Aggregates DRAFT from all section services; locale filtering |
| `src/main/java/.../preview/PreviewController.java` | GET `/api/v1/preview`, optional `locale` query param |

### 1.3 Relevant design docs

- **api-design.md §5.1** — Preview endpoint contract: path, auth, query params, response shape (envelope + data.hero, data.experiences, etc.; single-locale when `?locale=`).
- **phase1-mvp.md §6.2** — Draft Preview API: rationale (single payload, GraphQL-identical shape for component reuse), JWT (ADMIN), response structure.

### 1.4 Constraints and assumptions

- **Auth:** `/api/v1/**` already requires JWT (SecurityConfig); no extra “ADMIN” check beyond existing OAuth2 allowed-admins → JWT.
- **Data source:** Only DRAFT; all section services expose `getDraft()` (hero) or `list()` (sections), which read DRAFT only.
- **Locales:** Phase 1 supports `en` and `vi` only; design says “en or vi” for the query param.
- **Backward compatibility:** N/A (new endpoint).

---

## 2. Design Doc Summary

### 2.1 api-design.md §5.1 — Key decisions and constraints

| Decision / constraint | Implication for implementation |
|------------------------|---------------------------------|
| GET `/api/v1/preview` and GET `/api/v1/preview?locale=en` | One handler, optional query param. |
| Auth: JWT | Endpoint under `/api/v1/**`; no change to security. |
| Query param `locale`: optional, “en” or “vi” | When present and valid, return single-locale values; when omitted, return full multi-locale shape. |
| Response: unified envelope `success`, `data`, `timestamp` | Use existing `ApiResponse.success(data)`; `data` = PreviewResponse. |
| Response `data`: hero (object), experiences, projects, education, skills, certifications, socialLinks (arrays) | PreviewResponse must expose these seven keys; section order matches design. |
| No locale: hero has e.g. `tagline: { "en": "...", "vi": "..." }` | Use existing section DTOs (HeroResponse, etc.) that already have Map fields. |
| With `?locale=en`: “each field in data is the single-locale value” (e.g. `"company": "TechCorp"`) | Transform Map → String (and Map&lt;String, List&lt;String&gt;&gt; → List&lt;String&gt;) for that locale. |
| Example hero includes `profilePhotoUrl` | Design example uses `profilePhotoUrl`; Hero CRUD uses `profilePhotoMediaId`. Document or align. |

### 2.2 phase1-mvp.md §6.2 — Key decisions and constraints

| Decision / constraint | Implication for implementation |
|------------------------|---------------------------------|
| “All draft sections in a single response” | One service that calls all seven section services and assembles one payload. |
| “Structured identically to the GraphQL public response” | Same top-level keys and, with locale, same per-field shape (strings for localized fields). |
| “Frontend preview renderer can reuse the same components as the landing page” | Single-locale view must match GraphQL output shape (e.g. hero.tagline as string when locale set). |
| JWT (ADMIN) | Implemented as “any valid JWT for /api/v1/**”; admin-only enforced at OAuth2/JWT issue time. |

### 2.3 feature-core-cms.md (requirements)

- **Preview API:** “Backend must expose a way for the frontend to read **draft** content … so that a separate preview URL (e.g. `/preview`) can render the draft version of the site before publish.”
- **Implementation:** Single REST endpoint returning full draft payload satisfies this; no requirement for a separate “draft GraphQL” if REST preview exists.

---

## 3. File-by-File Comparison

### 3.1 PreviewResponse.java

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Top-level keys: hero, experiences, projects, education, skills, certifications, socialLinks | All seven fields present, same order as design. | ✅ Aligned |
| hero: object (map or single-locale shape) | `Object hero` (set to HeroResponse or Map for locale view). | ✅ Aligned |
| experiences, projects, etc.: arrays | `Object` for each; at runtime set to List of section DTOs or List of Maps (locale). | ✅ Aligned |
| Envelope (success, data, timestamp) | Handled by ApiResponse in controller; not in this DTO. | ✅ Correct |

**Notes:** Using `Object` for each section allows both “full” and “single-locale” shapes without separate DTO hierarchies. JSON output matches design. No logic gaps.

**Suggestions:** None required. Optional: add a short class-level comment that `data` in the API is this object (for OpenAPI generation clarity).

---

### 3.2 PreviewService.java

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Aggregate all DRAFT sections | Calls heroService.getDraft(), experienceService.list(), projectService.list(), educationService.list(), skillService.list(), certificationService.list(), socialLinkService.list(). | ✅ Aligned |
| Sections: hero, experiences, projects, education, skills, certifications, socialLinks | All seven; list methods return DRAFT-only (per existing services). | ✅ Aligned |
| Optional locale filter “en” or “vi” | isSingleLocale(locale) checks non-blank and locale in ["en","vi"] (case-insensitive). | ✅ Aligned |
| When locale set: single-locale value per field | toHeroLocale, toExperiencesLocale, toProjectsLocale, toEducationLocale, toSkillsLocale, toCertificationsLocale build Map or List of Maps with String values for localized fields; mapValue / mapListValue extract locale from Map. | ✅ Aligned |
| When locale omitted: full draft shape | Else branch sets response from section DTOs directly (HeroResponse, List&lt;ExperienceItemResponse&gt;, etc.). | ✅ Aligned |

**Deviations / choices:**

1. **Hero: profilePhotoMediaId vs profilePhotoUrl**  
   Design example (§5.1) shows `profilePhotoUrl`. Implementation uses `profilePhotoMediaId` to match Hero CRUD. **Recommendation:** Document in API design that preview (and future GraphQL) use `profilePhotoMediaId` until media resolution exists; then add optional `profilePhotoUrl` when URLs are available.

2. **Experience item: itemId vs id**  
   Design example shows `"id": "uuid-1"`. Implementation uses `itemId` everywhere (consistent with REST CRUD and other sections). **Recommendation:** Treat as intentional; align design doc to `itemId` for consistency across REST and preview/GraphQL.

3. **Invalid locale (e.g. ?locale=fr)**  
   Design: “en or vi”. Implementation: treats unsupported locale as “no locale” and returns full multi-locale payload. **Verdict:** Acceptable; design does not require 400. Optional improvement: validate and return 400 for invalid locale if product wants strict behavior.

**Edge cases:**

- **Null / missing draft:** heroService.getDraft() can return null; toHeroLocale(null, locale) returns null. Other list() methods return empty list. Response has null hero and empty arrays. Matches “no draft yet” scenario. ✅  
- **Missing locale key in map:** mapValue returns null; mapListValue returns empty list. Safe. ✅  
- **Null list in section DTO:** toXxxLocale and list streams handle null with `list == null ? List.of()` or equivalent. ✅  

**Security:** No direct DB or user input beyond `locale` string; aggregation is read-only from existing services. No injection risk. ✅  

**Simplification:** Logic is clear; extracting mapValue/mapListValue was a good choice. No refactor required. Unused import (SkillItemResponse) removed. ✅  

---

### 3.3 PreviewController.java

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| GET `/api/v1/preview` | @GetMapping("/preview"), @RequestMapping("/api/v1"). | ✅ Aligned |
| Optional `?locale=en` or `vi` | @RequestParam(required = false) String locale. | ✅ Aligned |
| JWT required | Path under `/api/v1/**`; SecurityConfig requires authenticated. | ✅ Aligned |
| Response 200 with envelope | ResponseEntity.ok(ApiResponse.success(data)). | ✅ Aligned |

**Notes:** No validation of locale in controller (delegated to service). No logic gaps. No security issues.

**Suggestions:** None required. Optional: add @Parameter(description = "en or vi") for OpenAPI if Springdoc is in use.

---

## 4. Gaps and Missing Pieces

| Item | Severity | Recommendation |
|------|----------|----------------|
| **Tests** | Medium | Add integration test(s) for GET /api/v1/preview: (1) with valid JWT, no locale → 200, body has hero + 6 arrays; (2) with JWT and ?locale=en → 200, hero.tagline is string; (3) without JWT → 401. Optional: unit test for PreviewService.getPreview(null) and getPreview("en") with mocked section services. |
| **API design doc** | Low | Update §5.1 example: use `itemId` instead of `id` for experience item; add a line that hero may expose `profilePhotoMediaId` (and later `profilePhotoUrl` when resolved). |
| **Invalid locale** | Low | If product wants strict behavior, validate locale in controller or service and return 400 for values other than en/vi. |

---

## 5. Summary and Recommended Next Steps

### 5.1 Alignment summary

- **Endpoint, auth, and envelope:** Match design (GET /api/v1/preview, JWT, ApiResponse envelope).
- **Payload structure:** Seven sections present; full vs single-locale behavior matches §5.1 and §6.2.
- **Data flow:** Read-only aggregation from existing DRAFT-only services; no new repositories; locale filtering implemented correctly.
- **Deviations:** (1) Hero uses `profilePhotoMediaId` (document in design); (2) list items use `itemId` (align design example to `itemId`).

### 5.2 Recommended next steps

1. **High:** Add integration test for GET /api/v1/preview (auth + optional locale) and, if desired, a focused unit test for PreviewService locale vs no-locale.
2. **Low:** Update api-design.md §5.1: example experience item field `id` → `itemId`; document hero `profilePhotoMediaId` / future `profilePhotoUrl`.
3. **Optional:** Validate `locale` query param (en/vi only) and return 400 for invalid value if product requires strict behavior.
4. **Optional:** Add OpenAPI/Springdoc annotations for /preview and locale param if the project documents REST with OpenAPI.

Phase G implementation is **aligned with design and requirements** with only minor documentation and test follow-ups.

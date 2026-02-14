# Phase I (GraphQL Public API) — Design vs Implementation Review

**Review date:** 2026-02-12  
**Scope:** Phase I implementation (GraphQL Public API)  
**Design refs:** `docs/ai/design/api-design.md` §8, `docs/ai/design/phase1-mvp.md` §6.1, §9.3  
**Requirements refs:** `docs/ai/requirements/feature-core-cms.md`, `docs/ai/requirements/feature-api-conventions.md`

---

## 1. Scope and Context

### 1.1 Feature / branch description

**Phase I (GraphQL Public API):** Single unauthenticated endpoint `POST /graphql` that exposes **PUBLISHED** content only. Root queries: hero, experiences, projects, education, skills, certifications, socialLinks, siteSettings. Optional `locale` argument (EN | VI); when omitted, use `siteSettings.defaultLocale`. List fields sorted by `order`; projects filtered to `visible: true`. Errors via custom `DataFetcherExceptionResolver` (message, path, extensions.code; no stack traces).

### 1.2 Modified / added files (Phase I)

| File | Purpose |
|------|---------|
| `src/main/resources/graphql/schema.graphqls` | GraphQL schema: Locale enum, Query, all types |
| `src/main/java/.../graphql/model/*.java` | GraphQL response types (Hero, ExperienceItem, ProjectItem, Link, EducationItem, SkillCategory, SkillItem, CertificationItem, SocialLinkItem, SiteSettings, Locale) |
| `src/main/java/.../graphql/ContentGraphQLController.java` | Root Query resolvers (@QueryMapping) |
| `src/main/java/.../config/GraphQLExceptionResolver.java` | DataFetcherExceptionResolverAdapter for GraphQL errors |
| `HeroService.java` | Added `getPublished()` |
| `ExperienceService.java`, `ProjectService.java`, `EducationService.java`, `CertificationService.java`, `SocialLinkService.java`, `SkillService.java` | Added `listPublished()` or `listPublishedVisible()` (projects only) |

### 1.3 Relevant design docs

- **api-design.md §8** — GraphQL endpoint, schema (enum, Query, types), locale/ordering rules, example LandingPage query, error format (errors array, extensions.code).
- **phase1-mvp.md §6.1** — Schema and locale/ordering; §9.3 — DataFetcherExceptionResolver, no internal details, partial data.

### 1.4 Constraints and assumptions

- **Auth:** None for `/graphql`; SecurityConfig permits `/graphql`, `/graphql/**`.
- **Data source:** PUBLISHED only; new service methods read from repositories with `ContentState.PUBLISHED`.
- **Locale:** Design uses enum Locale (EN, VI); backend maps to `"en"` / `"vi"` for Map lookups; default from SettingsService when argument omitted.
- **Backward compatibility:** New API; no breaking changes to REST.

---

## 2. Design Doc Summary

### 2.1 api-design.md §8 — Key decisions and constraints

| Decision / constraint | Implication for implementation |
|------------------------|---------------------------------|
| POST /graphql, no auth | Endpoint public; resolvers use only PUBLISHED data. |
| Queries only (no mutations) | Schema has only Query type; no Mutation. |
| Schema: Locale enum (EN, VI), Query with hero(locale?), experiences(locale?), … siteSettings | schema.graphqls must match; resolvers map to Java types. |
| Types: Hero (tagline, bio, fullName, title, profilePhotoUrl), ExperienceItem (id, company, role, …), etc. | GraphQL model types and resolvers must produce this shape. |
| Locale: if omitted, use siteSettings.defaultLocale | Resolver must call SettingsService when locale argument is null. |
| Ordering: list fields sorted by order ascending | Services already return sorted lists (listPublished / listPublishedVisible). |
| Projects: only visible == true | ProjectService.listPublishedVisible() filters by visible. |
| Error: errors array, message, path, extensions.code; no internal details | DataFetcherExceptionResolver must map exceptions and use safe messages. |
| Partial data when only some fields fail | Spring GraphQL + resolver support this by default. |

### 2.2 phase1-mvp.md §6.1, §9.3 — Key decisions and constraints

| Decision / constraint | Implication for implementation |
|------------------------|---------------------------------|
| Single endpoint POST /graphql; only PUBLISHED | Same as api-design. |
| Locale resolution from argument or default from settings | resolveLocale(locale) in controller. |
| List ordering and projects visible filter | Implemented in service layer. |
| DataFetcherExceptionResolver: structured errors, no stack traces / class names | GraphQLExceptionResolver overrides default behavior. |
| No auth errors for public GraphQL | Not applicable; no 401/403 from this API. |

### 2.3 Requirements (feature-core-cms, feature-api-conventions)

- **Visitor** reads only published content via GraphQL; **GraphQL has no auth; only published data**; **empty state**: GraphQL returns empty arrays or null until content exists.
- **Errors:** GraphQL errors array; partial data; no internal details. **Endpoint:** Single `/graphql` for v1.

---

## 3. File-by-File Comparison

### 3.1 schema.graphqls

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Enum Locale { EN, VI } | Present. | ✅ Aligned |
| Query: hero(locale?), experiences(locale?), projects(locale?), education(locale?), skills(locale?), certifications(locale?), socialLinks, siteSettings | All present; socialLinks has no locale arg (design). | ✅ Aligned |
| Types: Hero, ExperienceItem, ProjectItem, Link, EducationItem, SkillCategory, SkillItem, CertificationItem, SocialLinkItem, SiteSettings | All present; field names match design. | ✅ Aligned |
| Hero: tagline, bio, fullName, title, profilePhotoUrl | Present. | ✅ Aligned |
| ExperienceItem: id ID!, company, role, startDate, endDate, bulletPoints, techUsed, order | Present. | ✅ Aligned |
| ProjectItem: id, title, description, techStack, links, mediaUrls, visible, order | Present. | ✅ Aligned |
| SiteSettings: supportedLocales [String!]!, defaultLocale String! | Present. | ✅ Aligned |

**Notes:** Schema is a direct match to api-design §8.1. No deviations.

---

### 3.2 ContentGraphQLController.java

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| hero(locale): return Hero or null | hero() calls heroService.getPublished(); null → null; else map to Hero with locale-resolved scalars. | ✅ Aligned |
| experiences(locale): [ExperienceItem!]! | experiences() uses experienceService.listPublished(), maps to ExperienceItem with id=itemId, locale for company/role/bulletPoints. | ✅ Aligned |
| projects(locale): [ProjectItem!]! (visible only) | projects() uses projectService.listPublishedVisible(); maps to ProjectItem; mediaUrls set from mediaIds. | ✅ Aligned |
| education(locale), skills(locale), certifications(locale) | All use listPublished() and map with locale; skills items passed through. | ✅ Aligned |
| socialLinks: no locale argument | socialLinks() takes no argument; uses listPublished(). | ✅ Aligned |
| siteSettings: supportedLocales, defaultLocale | siteSettings() uses settingsService.getOrCreate(); maps to SiteSettings; null-safe for supportedLocales/defaultLocale. | ✅ Aligned |
| Locale when null → default from settings | resolveLocale(locale): if locale != null use locale.name().toLowerCase(); else get settings.getDefaultLocale() (fallback "en"). | ✅ Aligned |
| Empty state: null hero, empty lists | getPublished() null → hero returns null; listPublished() returns empty list → empty array. | ✅ Aligned |

**Deviations / choices:**

1. **profilePhotoUrl vs profilePhotoMediaId**  
   Design: `profilePhotoUrl: String`. Implementation: `out.setProfilePhotoUrl(r.getProfilePhotoMediaId())` — exposes media ID as the “URL” field until a media service exists. **Verdict:** Documented trade-off; align with Preview API (same choice). Optional: document in api-design that profilePhotoUrl may be an ID until URLs are resolved.

2. **mediaUrls**  
   Design: `mediaUrls: [String!]`. Implementation: returns `r.getMediaIds()` (IDs, not resolved URLs). Same as design intent for “media” until resolution exists. **Verdict:** Acceptable; document if needed.

3. **Error code NOT_FOUND vs RESOURCE_NOT_FOUND**  
   Design table §9.4 lists REST code `RESOURCE_NOT_FOUND`; GraphQL example §8.4 uses `"code": "NOT_FOUND"`. Implementation uses NOT_FOUND. **Verdict:** Matches GraphQL example; no change.

**Edge cases:**

- **Settings missing defaultLocale:** Fallback to `"en"` in controller and in siteSettings() for supportedLocales. ✅  
- **Missing locale key in map:** mapValue returns null; mapListValue returns empty list. ✅  
- **Null lists in responses:** All list mappings use `!= null ? … : List.of()`. ✅  

**Security:** No user input beyond GraphQL operation; locale is an enum. Resolvers only read PUBLISHED data. ✅  

---

### 3.3 GraphQLExceptionResolver.java

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Custom DataFetcherExceptionResolver | Extends DataFetcherExceptionResolverAdapter; resolveToSingleError returns GraphQLError. | ✅ Aligned |
| Errors: message, path, extensions.code | GraphqlErrorBuilder.newError(env).message(...).extension("code", code).build() — path comes from env. | ✅ Aligned |
| No stack traces or internal class names | Generic messages: "Resource not found", "Validation failed", "An unexpected error occurred"; no ex.getClass().getName(). | ✅ Aligned |
| Partial data when only some fields fail | Adapter resolves one error; Spring GraphQL continues with partial data. | ✅ Aligned |

**Notes:** ResourceNotFoundException → NOT_FOUND; ValidationException → VALIDATION_ERROR; all others → INTERNAL_ERROR. Design does not require handling other specific exceptions (e.g. INVALID_LOCALE) for GraphQL; locale is enum-constrained. ✅  

---

### 3.4 Service layer (getPublished / listPublished / listPublishedVisible)

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Hero: PUBLISHED only | HeroService.getPublished(): findByContentState(PUBLISHED), mapper to response. | ✅ Aligned |
| Experiences: PUBLISHED, sorted by order | ExperienceService.listPublished(): same pattern as list() but PUBLISHED; itemsSortedByOrder. | ✅ Aligned |
| Projects: PUBLISHED, visible only, sorted | ProjectService.listPublishedVisible(): filter isVisible(), then sort by order. | ✅ Aligned |
| Education, Certifications, SocialLinks, Skills: PUBLISHED, sorted | Each listPublished() uses findByContentState(PUBLISHED) and sorted order. | ✅ Aligned |

**Notes:** No logic gaps. All read-only; no new repositories. ✅  

---

### 3.5 Application config and security

| Design requirement | Implementation | Status |
|--------------------|----------------|--------|
| Path /graphql; schema location | application.yml: spring.graphql.path=/graphql, schema.locations=classpath:graphql/. | ✅ Aligned |
| GraphiQL in dev | application-dev.yml: graphiql.enabled: true. | ✅ Aligned |
| POST /graphql public | SecurityConfig: requestMatchers("/graphql", "/graphql/**").permitAll(). | ✅ Aligned |

---

## 4. Gaps and Missing Pieces

| Item | Severity | Recommendation |
|------|----------|----------------|
| **GraphQL integration tests** | Medium | Add test (e.g. WebTestClient or GraphQlTester) for POST /graphql: LandingPage query with variable locale EN; assert hero, experiences, etc. shape; test without auth. Optional: test error path (e.g. invalid query) and resolver exception mapping. |
| **Invalid / unsupported locale** | Low | Schema enum restricts to EN/VI; no INVALID_LOCALE from resolver. If a future scalar allowed other values, could add validation and map to extensions.code INVALID_LOCALE. |
| **API design doc** | Low | Optionally state in §8 that profilePhotoUrl may contain a media ID until a media URL service exists; same for mediaUrls in ProjectItem. |
| **Documentation** | Low | README or deployment doc: mention GraphiQL at /graphql when dev profile is active (if not already). |

---

## 5. Summary and Recommended Next Steps

### 5.1 Alignment summary

- **Endpoint, auth, schema:** Match design (POST /graphql, no auth, schema and types as §8).
- **Locale and ordering:** Resolved from argument or siteSettings.defaultLocale; lists sorted by order; projects visible-only.
- **Data flow:** PUBLISHED-only via new service methods; single controller with clear mapping to GraphQL types.
- **Errors:** DataFetcherExceptionResolver in place; safe messages and extensions.code; no internal leakage.
- **Deviations:** profilePhotoUrl and mediaUrls expose IDs until URL resolution exists (document as needed).

### 5.2 Recommended next steps

1. **High:** Add GraphQL integration test(s): LandingPage query with locale variable; assert structure and empty-state behavior; optionally test error handling.
2. **Low:** Update api-design §8 (or implementation note): profilePhotoUrl / mediaUrls may be IDs until media service is available.
3. **Optional:** Document GraphiQL usage in dev in README or `docs/ai/deployment/`.

Phase I implementation is **aligned with design and requirements**; remaining work is tests and minor documentation.

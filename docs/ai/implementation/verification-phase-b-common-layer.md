# Implementation Verification — Phase B (Common Layer)

**Feature/branch:** Phase B — Common Layer (M1 completion)  
**Relevant design:** `docs/ai/design/api-design.md`, `docs/ai/design/database-design.md`, `docs/ai/design/phase1-mvp.md`  
**Relevant requirements:** `docs/ai/requirements/feature-api-conventions.md`  
**Date:** 2026-02-12

---

## 1. Context

### 1.1 Feature Description

Phase B delivers the shared kernel for the Toby.Résumé backend: content state enum, base document with auditing, unified REST envelope (success/error), global exception handling, locale validation, CORS from config, and UUID generation for embedded items.

### 1.2 Modified / Created Files (Phase B)

| File | Purpose |
|------|--------|
| `common/model/ContentState.java` | Enum DRAFT, PUBLISHED |
| `common/model/BaseDocument.java` | Abstract base: id, createdAt, updatedAt |
| `config/MongoConfig.java` | @EnableMongoAuditing |
| `common/dto/ApiResponse.java` | Success envelope: success, data, timestamp |
| `common/dto/ErrorBody.java` | Error object: code, message, details (FieldErrorDetail) |
| `common/exception/GlobalExceptionHandler.java` | @RestControllerAdvice → envelope + HTTP status |
| `common/exception/ResourceNotFoundException.java` | 404 |
| `common/exception/UnauthorizedException.java` | 401 |
| `common/exception/ForbiddenException.java` | 403 |
| `common/exception/ValidationException.java` | 400, VALIDATION_ERROR + optional details |
| `common/validation/ValidLocaleKeys.java` | Annotation for "en"/"vi" only |
| `common/validation/LocaleKeysValidator.java` | Validator implementation |
| `config/CorsConfig.java` | CORS from app.cors.* |
| `common/util/IdGenerator.java` | uuid() for embedded item IDs |
| `config/SecurityConfig.java` | Updated to use .cors(Customizer.withDefaults()) |

### 1.3 Constraints / Assumptions

- Phase B does not implement auth; Security still permits all paths. Phase C will protect `/api/v1/**`.
- No GraphQL error handling in Phase B (design: separate DataFetcherExceptionResolver in Phase I).
- CORS applies to all paths; design does not require path-specific CORS.

---

## 2. Design Doc Summary — What Must Be Respected

### 2.1 api-design.md

- **§2 REST envelope:** All REST responses use one shape. Success: `success: true`, `data`, `timestamp` (ISO-8601 UTC). Error: `success: false`, `error: { code, message, details? }`, `timestamp`; `data` omitted.
- **§2.3 Error codes & HTTP status:** VALIDATION_ERROR→400, INVALID_LOCALE→400, UNAUTHORIZED→401, FORBIDDEN→403, RESOURCE_NOT_FOUND→404, PUBLISH_FAILED→500, INTERNAL_ERROR→500. Validation 400 must support `error.details` as array of `{ field, message }`.
- **§1.2 CORS:** Origins from config; methods GET, POST, PUT, DELETE, OPTIONS; headers Content-Type, Authorization; credentials true.

### 2.2 database-design.md

- **§4.1 ContentState:** Enum DRAFT, PUBLISHED; stored as string in MongoDB.
- **§4.2 BaseDocument:** All content/settings documents extend it. Fields: _id (ObjectId), createdAt (DateTime), updatedAt (DateTime). Auditing sets createdAt/updatedAt automatically.
- **§3 Locale keys:** Only "en" and "vi" valid for Map keys; validated at DTO layer.
- **UUID for embedded items:** Server-generated UUID strings for embedded item IDs (not ObjectId).

### 2.3 phase1-mvp.md

- **§3 Package structure:** common/exception (GlobalExceptionHandler, custom exceptions), common/model (ContentState, BaseDocument), common/dto (ApiResponse, error branch), common/validation (ValidLocaleKeys, LocaleKeysValidator), common/util (ID generation), config (CorsConfig, MongoConfig).
- **§9 Exception handling:** Map exceptions to error envelope and HTTP status; validation details in error.details.
- **§10 CORS:** From app.cors.* (origins, methods, headers, credentials).

### 2.4 feature-api-conventions.md (requirements)

- **Error body:** Consistent shape with code, message, optional details; field-level validation in details.
- **HTTP status:** 400 validation, 401 auth, 403 forbidden, 404 not found, 500 internal.
- **CORS:** Admin Panel and Landing Page origins; credentials allowed; methods include GET, POST, PUT, PATCH, DELETE, OPTIONS; Content-Type, Authorization.

---

## 3. File-by-File Comparison

### 3.1 common/model/ContentState.java

| Design | Implementation | Status |
|--------|----------------|--------|
| Enum DRAFT, PUBLISHED | Same | ✅ Match |
| Stored as string in MongoDB | Default enum serialization is name() → "DRAFT"/"PUBLISHED" | ✅ Match |

**Verdict:** Matches database-design §4.1.

---

### 3.2 common/model/BaseDocument.java

| Design | Implementation | Status |
|--------|----------------|--------|
| _id ObjectId | `String id` with @Id | ⚠️ Minor deviation |
| createdAt DateTime, @CreatedDate | `Instant createdAt` @CreatedDate | ✅ Match (Instant = UTC) |
| updatedAt DateTime, @LastModifiedDate | `Instant updatedAt` @LastModifiedDate | ✅ Match |

**Note:** Design table says `_id` type ObjectId. Spring Data MongoDB accepts `String` for `@Id` and maps it to MongoDB ObjectId (serialized as string in JSON). Common practice; no schema break. If strict ObjectId type is required later, can switch to `org.bson.types.ObjectId`.

**Verdict:** Aligned; id type is an acceptable implementation choice.

---

### 3.3 config/MongoConfig.java

| Design | Implementation | Status |
|--------|----------------|--------|
| Enable MongoDB auditing | @Configuration, @EnableMongoAuditing | ✅ Match |

**Verdict:** Matches database-design §4.2 and phase1-mvp §3.

---

### 3.4 common/dto/ApiResponse.java & ErrorBody.java

| Design | Implementation | Status |
|--------|----------------|--------|
| Success: success, data, timestamp | ApiResponse.success(data) → success=true, data, timestamp=Instant.now() | ✅ Match |
| Error: success, error, timestamp; no data | ApiResponse.error(ErrorBody) → success=false, error, timestamp; data null, @JsonInclude(NON_NULL) omits | ✅ Match |
| error: code, message, details (optional) | ErrorBody(code, message, details); FieldErrorDetail(field, message) | ✅ Match |
| timestamp ISO-8601 UTC | Instant.now().toString() | ✅ Match |

**Naming:** Design doc mentions "ErrorResponse" for the error branch; implementation uses **ErrorBody** for the `error` object (nested in ApiResponse). JSON shape is identical.

**Verdict:** Matches api-design §2.1, §2.2 and feature-api-conventions.

---

### 3.5 common/exception/GlobalExceptionHandler.java

| Design | Implementation | Status |
|--------|----------------|--------|
| @ControllerAdvice maps to envelope + status | @RestControllerAdvice, ResponseEntity<ApiResponse<Void>> | ✅ Match |
| 400 validation, details in error.details | MethodArgumentNotValidException → 400, VALIDATION_ERROR, details from field errors | ✅ Match |
| 401 UNAUTHORIZED | UnauthorizedException → 401, UNAUTHORIZED | ✅ Match |
| 403 FORBIDDEN | ForbiddenException → 403, FORBIDDEN | ✅ Match |
| 404 RESOURCE_NOT_FOUND | ResourceNotFoundException → 404, RESOURCE_NOT_FOUND | ✅ Match |
| 500 INTERNAL_ERROR | Exception → 500, INTERNAL_ERROR, generic message | ✅ Match |
| No internal details in 500 | "An unexpected error occurred"; stack only in logs | ✅ Match |

**Gaps / notes:**

- **INVALID_LOCALE (400):** Design has a distinct code `INVALID_LOCALE` for locale key not in ["en","vi"]. Currently `@ValidLocaleKeys` failures go through Bean Validation → `MethodArgumentNotValidException` → `VALIDATION_ERROR`. So locale violations are 400 with VALIDATION_ERROR, not INVALID_LOCALE. **Recommendation:** Optional enhancement: detect locale-specific constraint (e.g. message or custom property) and return code INVALID_LOCALE, or document that locale errors use VALIDATION_ERROR.
- **AccessDeniedException:** Not handled. When Phase C enables security, Spring Security may throw `AccessDeniedException` for 403. **Recommendation:** In Phase C, add handler for `AccessDeniedException` → 403 FORBIDDEN (or rely on security entry point; if REST returns JSON, handler is cleaner).
- **ValidationException.getCode():** Always "VALIDATION_ERROR"; no other code used. Aligned with design.

**Verdict:** Matches api-design §2.3 and phase1-mvp §9. Two optional follow-ups: INVALID_LOCALE code, AccessDeniedException in Phase C.

---

### 3.6 common/exception (ResourceNotFoundException, UnauthorizedException, ForbiddenException, ValidationException)

| Design | Implementation | Status |
|--------|----------------|--------|
| Custom exceptions for 404, 401, 403, 400 | All four present; ValidationException carries optional details | ✅ Match |

**Verdict:** Matches plan B.5; ForbiddenException is design §2.3 (403).

---

### 3.7 common/validation/ValidLocaleKeys.java & LocaleKeysValidator.java

| Design | Implementation | Status |
|--------|----------------|--------|
| Only "en" and "vi" allowed | ALLOWED = Set.of("en", "vi"); reject other keys | ✅ Match |
| Apply to Map fields (locale-keyed) | @ValidLocaleKeys on Map<?,?>; null/empty allowed | ✅ Match |

**Edge case:** Non-String keys (e.g. integer) rejected as invalid. Design only allows "en"/"vi" strings, so this is correct.

**Verdict:** Matches database-design §3 and phase1-mvp §3.

---

### 3.8 config/CorsConfig.java

| Design | Implementation | Status |
|--------|----------------|--------|
| Origins from app.cors.allowed-origins | @Value List<String> allowedOrigins | ✅ Match |
| Methods GET, POST, PUT, DELETE, OPTIONS | allowed-methods (default same), split by comma | ✅ Match |
| Headers Content-Type, Authorization | allowed-headers (default same), split by comma | ✅ Match |
| Credentials true | allow-credentials, default true | ✅ Match |
| max-age | maxAge configurable, default 3600 | ✅ Match |
| Applied to all paths | registerCorsConfiguration("/**", config) | ✅ Match |

**Requirements:** feature-api-conventions mentions PATCH. api-design §1.2 does not list PATCH. Implementation follows api-design (no PATCH). **Recommendation:** Add PATCH to default allowed-methods if frontend or future endpoints need it; otherwise leave as-is.

**application.yml:** Has `allowed-methods: GET, POST, PUT, DELETE, OPTIONS` (single string). CorsConfig splits by comma; no YAML list change required. allowed-origins is already a list.

**Verdict:** Matches api-design §1.2 and phase1-mvp §10.

---

### 3.9 common/util/IdGenerator.java

| Design | Implementation | Status |
|--------|----------------|--------|
| UUID for embedded item IDs | UUID.randomUUID().toString() | ✅ Match |

**Verdict:** Matches database-design (UUID for embedded items) and plan B.8.

---

### 3.10 config/SecurityConfig.java (Phase B change)

| Design | Implementation | Status |
|--------|----------------|--------|
| CORS applied in security chain | .cors(Customizer.withDefaults()) | ✅ Match |

**Verdict:** CorsConfigurationSource bean from CorsConfig is used; matches phase1-mvp intent.

---

## 4. Summary of Findings

### 4.1 Aligned with Design

- ContentState, BaseDocument, MongoConfig auditing.
- ApiResponse + ErrorBody (success/error envelope, timestamp, field details).
- GlobalExceptionHandler: 400, 401, 403, 404, 500 and codes; validation details; no internal leak on 500.
- Custom exceptions (including ForbiddenException).
- @ValidLocaleKeys and LocaleKeysValidator ("en", "vi" only).
- CorsConfig from app.cors.* and SecurityConfig CORS integration.
- IdGenerator.uuid().

### 4.2 Minor Deviations / Choices

- **BaseDocument.id:** Design says ObjectId; implementation uses `String`. Acceptable with Spring Data MongoDB; document in design or ADR if strict ObjectId is ever required.
- **ErrorBody vs ErrorResponse:** Naming only; JSON and behavior match design.
- **INVALID_LOCALE:** Locale validation errors currently return VALIDATION_ERROR. Optional: map to INVALID_LOCALE for consistency with api-design §2.3.
- **PATCH in CORS:** Not in api-design; in requirements. Implementation follows design; add PATCH later if needed.

### 4.3 Gaps / Risks

- **Tests:** No unit or integration tests for Phase B. Risk: regressions when changing handler, CORS, or validator.
- **AccessDeniedException:** Not handled; add in Phase C when securing `/api/v1/**`.
- **PUBLISH_FAILED:** No exception class yet; expected in Phase H (PublishService). Handler can be extended then.

### 4.4 Security & Edge Cases

- 500 responses do not expose stack traces or internal messages.
- CORS is config-driven; no hardcoded origins.
- Locale validator rejects non-"en"/"vi" keys and non-String keys.
- GlobalExceptionHandler catches generic Exception last; no unhandled throwable for REST controllers.

---

## 5. Recommended Next Steps

### High priority

1. **Add Phase B tests**
   - Unit: `LocaleKeysValidator` (valid/invalid keys, null, empty, non-String key).
   - Unit: `IdGenerator.uuid()` (format, uniqueness in bulk).
   - Integration: `GlobalExceptionHandler` (e.g. trigger ValidationException, ResourceNotFoundException, MethodArgumentNotValidException; assert status, envelope, and error.details).
   - Optional: integration test for CORS (e.g. Origin header, preflight).

### Medium priority (when touching related code)

2. **Phase C:** Add `@ExceptionHandler(AccessDeniedException.class)` → 403 FORBIDDEN with ApiResponse envelope.
3. **Optional:** Map `@ValidLocaleKeys` violations to error code `INVALID_LOCALE` (e.g. custom ConstraintViolation or message check in handler).

### Low priority / documentation

4. **Design doc:** In database-design §4.2, note that Java model may use `String id` with Spring Data mapping to MongoDB ObjectId.
5. **CORS:** If requirements (PATCH) are adopted, add PATCH to `app.cors.allowed-methods` default in CorsConfig and/or application.yml.

---

## 6. Verification Checklist

- [x] All Phase B tasks (B.1–B.8) implemented.
- [x] Envelope and error codes match api-design §2.
- [x] ContentState and BaseDocument match database-design §4.
- [x] Locale validation and IdGenerator match database-design §3 and plan.
- [x] CORS and SecurityConfig match api-design §1.2 and phase1-mvp §10.
- [ ] Unit/integration tests for Phase B (recommended).
- [ ] Phase C: add AccessDeniedException handling when securing REST.

**Overall:** Phase B implementation is **aligned with design and requirements**. Remaining work is tests, optional INVALID_LOCALE/PATCH, and Phase C exception handling.

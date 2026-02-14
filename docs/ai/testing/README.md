---
phase: testing
title: Testing Strategy
description: Define testing approach, test cases, and quality assurance
---

# Testing Strategy

## Test Coverage Goals
**What level of testing do we aim for?**

- Unit test coverage target: 80%+ for services (Phase K); 100% for new/changed code where practical.
- Integration test scope: critical REST and GraphQL paths, auth (401 without token), error handling.
- Alignment with requirements/design acceptance criteria.

## Testcontainers (Phase K)
**How do we run integration tests against a real database?**

- **MongoDB:** Each integration test class uses `@Testcontainers` and a `@Container static MongoDBContainer mongo = new MongoDBContainer("mongo:7")`.
- **Dynamic properties:** `@DynamicPropertySource` sets `spring.data.mongodb.uri` to the container’s replica set URL.
- **Isolation:** Each test class gets its own container lifecycle; tests within a class share the same container.
- **Run:** `mvn test` (Testcontainers must be able to pull/run the image; Docker or compatible runtime required).

## Unit Tests
**What individual components need testing?**

- [x] **HeroService** — getDraft (null/exists), upsertDraft (create/update).
- [x] **ExperienceService** — list, get, add, update, delete, reorder (see ExperienceServiceTest).
- [x] **ProjectService** — list, get, add, update, delete, reorder (see ProjectServiceTest).
- [x] **SettingsService** — getOrCreate (exists vs bootstrap), update (validation, valid save) (see SettingsServiceTest).
- [x] **PreviewService** — getPreview (null locale = full shape, "en" = single-locale hero, invalid locale = full) (see PreviewServiceTest).
- [ ] **EducationService, CertificationService, SocialLinkService, SkillService** — unit tests (pattern as above).
- [ ] **PublishService** — unit tests for publish flow and getStatus (optional; covered by PublishControllerIntegrationTest).

## Integration Tests
**How do we test component interactions?**

- [x] **HeroControllerIntegrationTest** — 401 without auth; GET/PUT with @WithMockUser; empty and with data.
- [x] **ExperienceControllerIntegrationTest** — CRUD + reorder, 401.
- [x] **ProjectControllerIntegrationTest** — CRUD + reorder, 401.
- [x] **PublishControllerIntegrationTest** — POST publish, GET status, 401.
- [x] **PreviewControllerIntegrationTest** — GET /api/v1/preview 401 without auth; 200 with @WithMockUser; optional locale.
- [x] **GraphQLIntegrationTest** — POST /graphql siteSettings and hero(locale: EN) queries; no auth required.
- [ ] **SettingsController, Education, Certification, SocialLink, Skill** — integration tests (same pattern: Testcontainers + MockMvc + @WithMockUser).

## End-to-End Tests
**What user flows need validation?**

- [ ] User flow 1: [Description]
- [ ] User flow 2: [Description]
- [ ] Critical path testing
- [ ] Regression of adjacent features

## Test Data
**What data do we use for testing?**

- Test fixtures and mocks
- Seed data requirements
- Test database setup

## Test Reporting & Coverage
**How do we verify and communicate test results?**

- **Run all tests:** `mvn test`
- **Run a single test class:** `mvn test -Dtest=HeroServiceTest` or `-Dtest=PreviewControllerIntegrationTest`
- **Coverage:** Use JaCoCo or similar (e.g. `mvn test jacoco:report`) for coverage reports; target 80%+ for services.

## Manual Testing
**What requires human validation?**

- **REST:** Use Postman/Insomnia or curl with JWT for `/api/v1/**` endpoints.
- **GraphQL:** With dev profile, **GraphiQL** is available at `http://localhost:8080/graphql` (or the app’s base URL). Use it to run LandingPage-style queries and inspect the schema.
- **OpenAPI:** With Springdoc enabled, **Swagger UI** at `/swagger-ui.html` and **OpenAPI JSON** at `/v3/api-docs` for REST contract discovery.
- Smoke tests after deployment: health, auth/me, one REST and one GraphQL query.

## Performance Testing
**How do we validate performance?**

- Load testing scenarios
- Stress testing approach
- Performance benchmarks

## Bug Tracking
**How do we manage issues?**

- Issue tracking process
- Bug severity levels
- Regression testing strategy


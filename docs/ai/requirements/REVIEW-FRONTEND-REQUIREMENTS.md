# Frontend Requirements Review

**Purpose:** Review FE project requirements against the project-level template (`docs/ai/requirements/README.md`) and BE requirements. Ensure structure and content alignment, and identify gaps or deviations.

**Reviewed:** `docs/ai/frontend/` (README.md, ui-ux-requirements.md, api-reference.md) and BE feature docs (feature-core-cms, feature-api-conventions, feature-extras, etc.).

**Date:** 2026-02-14

---

## 1. Template vs Current State

| Template section (`docs/ai/requirements/README.md`) | Where it appears for FE | Alignment |
|----------------------------------------------------|-------------------------|-----------|
| **Problem Statement** (core problem, who is affected, current situation) | Not in one place; implied in frontend README and core-cms | **Gap:** No single FE problem statement. |
| **Goals & Objectives** (primary, secondary, non-goals) | Partially in ui-ux (Out of Scope) and frontend README purpose | **Partial:** Goals scattered; non-goals only in "Out of Scope (Phase 1)". |
| **User Stories & Use Cases** | ui-ux-requirements (flows, screens, checklist) | **Good:** Flows and checklist cover stories; not phrased as "As a… I want…". |
| **Success Criteria** (measurable, acceptance, performance) | Not explicit for FE | **Gap:** No FE-specific success criteria doc. |
| **Constraints & Assumptions** | Scattered (tech in README, Phase 1 in ui-ux) | **Partial:** No dedicated section. |
| **Questions & Open Items** | None in frontend docs | **Gap:** No open questions for FE. |

**Conclusion:** The FE is not documented as a single `feature-frontend.md` in `docs/ai/requirements/`. It lives in `docs/ai/frontend/` with a different structure (index + UI/UX + API reference). Content is largely aligned with BE but does not follow the requirements template.

---

## 2. Core Problem Statement and Affected Users (FE)

**Inferred from BE + frontend docs:**

- **Core problem:** Toby needs two frontend applications: (1) an **Admin Panel** to manage portfolio content (draft/edit/preview/publish) securely, and (2) a **Landing Page** for visitors to view the published portfolio in EN/VI. Without these, the BE CMS and APIs cannot be used.
- **Affected users:**
  - **Toby (admin):** Must have a usable, secure UI for CRUD, preview, publish, and settings.
  - **Visitors:** Must see a fast, accessible, localized public portfolio.
- **Current situation:** FE docs describe the desired state; no existing FE codebase is referenced in the BE repo (separate repos assumed).

**Gap:** This problem statement is not written explicitly in any FE doc. Recommend adding a short "Problem Statement" in `docs/ai/frontend/README.md` or in a new `feature-frontend.md`.

---

## 3. Goals, Non-Goals, and Success Criteria

### Goals (inferred / from docs)

- **Primary:** Admin Panel (React): OAuth2 (Google/GitHub), CRUD for all sections, draft preview, publish, settings (default locale, PDF section visibility). Landing Page (Next.js): read-only published content via GraphQL, en/vi locale switching, responsive and accessible.
- **Secondary:** Clear error handling, empty/loading states, validation aligned with API, same layout for preview and landing.
- **Non-goals (Phase 1):** Media upload, PDF trigger, contact form, version history/rollback UI, AI translation/tailoring (ui-ux §4).

### Success Criteria (missing in FE docs)

- **Measurable:** e.g. Admin can complete login → edit section → preview → publish in one flow; Landing Page loads with one GraphQL query; locale switch works.
- **Acceptance:** Checklist in ui-ux §5 is the closest; not labeled as formal acceptance criteria.
- **Performance:** Only BE targets stated (REST p95 &lt; 500ms, GraphQL &lt; 300ms). No FE targets (e.g. LCP, FCP, TTI).

**Recommendation:** Add a "Success Criteria" subsection to the frontend README or to a `feature-frontend.md`, and optionally reference BE performance targets as API constraints.

---

## 4. Primary User Stories and Critical Flows

### From BE (feature-core-cms) vs FE (ui-ux-requirements)

| User story (BE) | FE coverage |
|-----------------|-------------|
| Admin: log in with Google or GitHub | §1.2 Auth flow, callback, token storage. |
| Admin: create/edit Hero, Experience, Projects, Education, Skills, Certifications, Social Links | §1.4 Content section UIs, list + form + reorder. |
| Admin: edits in draft first, live preview | §1.5 Preview (`GET /api/v1/preview?locale=…`). |
| Admin: publish draft → landing + PDF updated, version snapshot | §1.6 Publish (confirm, `POST /api/v1/publish`). |
| Admin: roll back to previous version | Out of scope Phase 1 (§4). |
| Visitor: landing page reads only published via GraphQL | §2.2 Data source, §2.3–2.4 sections. |

**Critical flows (FE):**

1. **Auth:** Unauthenticated → Login → OAuth redirect → callback with token → store token → Dashboard. 401/403 → clear token, redirect to login.
2. **Edit and publish:** Dashboard → section (e.g. Experience) → add/edit/delete/reorder → save → Preview (new tab) → Publish (confirm) → success/error.
3. **Landing:** Load page → one GraphQL query with locale → render sections → language switcher → re-query or update with new locale.

**Alignment:** User stories and critical flows are aligned between BE and FE. FE doc is implementation-oriented (screens, endpoints) rather than story-formatted.

---

## 5. Constraints, Assumptions, and Open Questions

### Constraints (from BE + FE docs)

- **Technical:** Admin Panel = React; Landing Page = Next.js; REST prefix `/api/v1/`; GraphQL `POST /graphql`; locales `en`, `vi` only; OAuth2 + JWT for admin.
- **Business:** Single admin (Toby); no multi-tenant; no billing.
- **API:** CORS and allowed origins for Admin Panel and Landing Page (feature-api-conventions).

### Assumptions (inferred)

- Backend is running and reachable; base URL configurable (e.g. localhost:8080 in dev).
- JWT stored per session (e.g. sessionStorage in dev; avoid localStorage in prod if possible).
- Preview reuses Landing Page layout/components with draft payload.
- Theming/layout are frontend-owned (feature-core-cms).

### Open Questions (missing in FE docs)

- **Auth:** Production token storage (httpOnly cookie vs sessionStorage) and refresh strategy.
- **Preview:** Same repo as Landing Page or separate; exact route and deployment.
- **Landing:** Single-page vs multi-page; section order (backend vs frontend).
- **SEO (Phase 1):** ui-ux says "Basic meta tags… Not required for MVP" — confirm if any meta is in scope.
- **Error reporting:** Whether to send client errors to a backend or third party.

**Recommendation:** Add a "Constraints & Assumptions" and "Questions & Open Items" section to the frontend README or to `feature-frontend.md`.

---

## 6. Missing Sections or Deviations from Template

| Item | Status |
|------|--------|
| Single FE requirements doc following template | **Missing.** No `feature-frontend.md` in `docs/ai/requirements/`. |
| Explicit Problem Statement for FE | **Missing** in frontend docs. |
| Goals / Non-goals in one place | **Partial.** Non-goals in ui-ux §4; goals implied. |
| User stories in "As a… I want… so that…" form | **Deviation.** FE uses flows and checklists, not story format. |
| Success criteria (measurable, acceptance, performance) | **Missing** for FE. |
| Constraints & Assumptions (dedicated) | **Missing** for FE. |
| Questions & Open Items | **Missing** for FE. |
| YAML front matter (phase, title, description) | **Present** in frontend README and ui-ux, api-reference. |

---

## 7. Gaps and Contradictions

### Gaps

1. **No `feature-frontend.md`:** FE is the only "feature" not in `docs/ai/requirements/` as a template-style doc.
2. **Success criteria:** No FE-specific acceptance or performance criteria.
3. **Open questions:** None listed for FE; only BE feature docs have "Questions & Open Items."
4. **Extras/PDF/Contact:** feature-extras (media, SEO, analytics, contact, webhooks) and feature-pdf-generation define backend behavior; FE ui-ux §4 correctly marks media, PDF trigger, contact as Phase 1 out of scope. No FE requirements yet for future phases (e.g. contact form UI, PDF download button).
5. **API reference vs BE:** api-reference is consistent with feature-core-cms and feature-api-conventions. Public contact path `POST /api/public/contact` (feature-extras) is not in the FE api-reference — acceptable while contact is Phase 3.

### Contradictions

- **None found.** FE docs are consistent with BE requirements (core-cms, api-conventions). Phase 1 out-of-scope in ui-ux matches BE phasing.

### Minor inconsistencies

- **GraphQL locale:** api-reference uses `Locale!` and `EN`/`VI`; BE content schema uses `en`/`vi`. Confirm enum is `EN`/`VI` in GraphQL and `en`/`vi` in REST (as in api-reference).
- **Preview response shape:** api-reference §2.9 shows `profilePhotoUrl` in preview; core-cms Hero has `profilePhotoMediaId`. FE receives resolved `profilePhotoUrl` from preview endpoint — ensure BE design doc matches.

---

## 8. Suggested Clarifications and Additions

1. **Add `docs/ai/requirements/feature-frontend.md`** (optional but recommended): One doc that follows the template with Problem Statement, Goals & Non-goals, User Stories (as "As a… I want…"), Success Criteria, Constraints & Assumptions, and Questions & Open Items. Keep it high-level; keep ui-ux-requirements and api-reference as the detailed references.
2. **Or enhance `docs/ai/frontend/README.md`:** Add short sections: Problem Statement, Goals & Non-goals, Success Criteria, Constraints & Assumptions, Open Questions. Link to BE requirements for traceability.
3. **Success criteria:** Define 3–5 acceptance criteria for Admin Panel and 3–5 for Landing Page (e.g. "Admin can publish draft and see success with versionId"; "Landing page renders all sections for chosen locale").
4. **Open questions:** Add 5–10 open items (auth storage, preview deployment, SEO scope, error reporting, etc.) and assign owners or "TBD."
5. **Future phases:** In FE docs, add one line per BE feature (extras, PDF, AI) stating when FE work is expected (e.g. "Contact form UI: Phase 3, see feature-extras").
6. **Cross-links:** In `docs/ai/requirements/README.md`, add a note that FE requirements live in `docs/ai/frontend/` and optionally in a future `feature-frontend.md`.

---

## 9. Summary

| Aspect | Verdict |
|--------|--------|
| **Structure vs template** | FE docs do not follow the requirements template; no single feature-style doc. |
| **Content vs BE** | Aligned: core-cms, api-conventions, phasing (Phase 1 out of scope). |
| **Problem / users / goals** | Inferred and partially stated; not in one place. |
| **User stories & flows** | Covered by flows and checklist; not in story format. |
| **Success criteria** | Missing for FE. |
| **Constraints / assumptions** | Scattered; no dedicated section. |
| **Open questions** | Missing for FE. |
| **Gaps** | No feature-frontend.md; no FE success criteria; no FE open questions. |
| **Contradictions** | None. |

**Recommendation:** Either create `feature-frontend.md` in `docs/ai/requirements/` following the template and keep `docs/ai/frontend/` as the implementation reference, or add the missing template sections (Problem Statement, Goals, Success Criteria, Constraints & Assumptions, Open Questions) to `docs/ai/frontend/README.md` and add a pointer from `docs/ai/requirements/README.md` to the frontend docs.

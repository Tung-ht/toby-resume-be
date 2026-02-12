# PROJECT REQUIREMENTS SPECIFICATION: TOBY.RÉSUME

## 1. Project Overview

A Personal Portfolio Management System (Headless CMS) designed to build multi-language content and leverage AI to optimize resume content based on specific Job Descriptions (JD). The system is **single-user** (Toby only). **This repository is API-only:** the Admin Panel and Landing Page are built as separate frontend projects. The backend exposes a **REST API** for the Admin Panel and a **GraphQL API** for the public Landing Page; both must be complete and well-documented for consumption by the frontends.

## 2. Tech Stack

| Decision | Choice |
|----------|--------|
| Backend Framework | Spring Boot 3.x (Java 17+) |
| Database | MongoDB |
| AI Provider | Google Gemini |
| Authentication | OAuth2 (Google + GitHub) |
| Admin API | REST |
| Public API | GraphQL (landing page queries only) |
| Media Storage | Local filesystem (Docker volume) |
| PDF Engine | OpenPDF (pure Java) |
| Deployment | Docker / Docker Compose |
| User Scope | Single-user (Toby only) |

## 3. Content Schema (Data Model)

The CMS manages the following content sections. All sections support **multi-language** storage (default: English and Vietnamese).

- **Hero / About Me** — Intro, tagline, bio, profile photo reference
- **Work Experience** — Company, role, dates, bullet points, tech used
- **Projects / Portfolio** — Title, description, tech stack, links, screenshots
- **Education** — Institution, degree, dates, details
- **Skills / Tech Stack** — Categories, proficiency levels
- **Certifications & Awards** — Title, issuer, date, link
- **Social Links** — Platform, URL, icon

Detailed field definitions and validation rules are in `docs/ai/requirements/feature-core-cms.md`.

## 4. Authentication & Authorization

- **OAuth2** login via **Google** and **GitHub** for the Admin Panel.
- Session/token management; role concept for API security (admin vs public).
- **Public API** (GraphQL): no auth required for landing page data.
- **Protected API** (REST): all admin CRUD requires valid OAuth2 session/token.

## 5. Core Features

### A. AI-Assisted Multi-language Management

- **i18n Data Structure:** The backend supports multi-language storage for **English (EN) and Vietnamese (VI) only**. No additional languages are in scope.
- **AI Auto-Translate:** In the Admin Dashboard, an **"AI Translate"** button uses **Google Gemini** to translate content between languages while preserving Markdown/HTML formatting. Users can manually override AI translations.
- **Fallback:** If AI translation fails, the system retains the source content and surfaces an error; user can retry or edit manually.
- **Language Switching:** Seamless language toggle for visitors on the Landing Page.

### B. AI Resume Tailoring (The "Killer" Feature)

- **Input:** Users paste a Job Description (JD) into the Admin Panel. JDs can be saved for reference.
- **AI Suggestion (Gemini):** The AI analyzes key terms from the JD, compares them with the existing profile, and provides actionable insights:
  - Recommends which projects to prioritize or move to the top.
  - Suggests rewrites for experience bullet points to align with the target company's tech stack.
  - May suggest **adding new content** (e.g. new bullet points, skills to add) for the user to review.
  - Scope may extend to skills emphasis and bio tone (configurable).
- **One-click Apply (Phase 2):** User applies AI suggestions to the **main draft** (no named JD drafts yet). **Phase 3:** JD-specific draft variants (named draft per JD); user can publish, promote to main, or discard each.
- **AI Cover Letter:** For each JD, the AI can also generate a **cover letter** (editable text in the admin panel; admin copies/pastes as needed). Same phase as Resume Tailoring.
- **JD Storage:** Past JDs are stored for reference and re-use.

### C. Draft & Published Management

- **Staging Area:** All modifications to **content** are saved as a **whole-site Draft** by default, with per-section edit capability. (Theming/layout is a frontend concern; the backend does not store theme settings.) **Phase 2:** One main draft only. **Phase 3:** Main draft plus multiple JD-specific drafts (from AI tailoring).
- **Live Preview:** A separate preview URL (e.g. served by the frontend) can render draft content; the backend exposes draft data via API so the frontend can display it before publish.
- **Publishing Pipeline:** On "Publish," data is synchronized from Draft to Production, a **version snapshot** is stored, and a fresh PDF is generated. Optional **draft expiration / cleanup** policy for old drafts.
- **Version History:** Version snapshots on every publish; rollback to any previous published version; optional version diff/comparison. See `docs/ai/requirements/feature-core-cms.md`.

### D. Dynamic PDF Generation

- **Engine:** **OpenPDF** (pure Java) — no browser dependency. PDFs are generated from structured content, not web-to-PDF.
- **Templates:** Support for different CV layouts/styles (template management).
- **Localized Exports:** PDF content follows the selected language (e.g., viewing English content exports an English PDF). Layout is LTR for EN/VI.
- **Caching:** PDFs are regenerated only when content changes.
- **Public Download:** Visitors can download the CV PDF directly from the landing page (public download button; backend exposes a public download endpoint).

## 6. Additional Features

- **Media Management:** Upload API for images (profile photo, project screenshots). Local filesystem storage (Docker volume). Image validation (size, format), optional thumbnails, media library for reuse.
- **SEO Metadata:** Per-page meta title, description, keywords; Open Graph tags; JSON-LD (Person schema); sitemap generation.
- **Analytics:** Basic page view tracking; dashboard for visits, top pages, referrers; optional Google Analytics integration hook.
- **Contact Form:** Public form on landing page; messages stored in DB; one-way (visitor sends, admin reads); email notification to admin via SMTP on new message; spam protection (rate limiting, honeypot, optional CAPTCHA).
- **Webhooks:** Configurable webhook URLs triggered on publish (e.g., frontend rebuild, Telegram/Discord).

## 7. API Design

- **REST API** for all admin CRUD operations (content, media, settings, publish, AI actions). Must be complete and well-documented for the separate Admin Panel frontend.
- **GraphQL API** for public/landing page data consumption only. Must be complete and well-documented for the separate Landing Page frontend.
- **API conventions:** Error response format, HTTP status usage, versioning, CORS, and rate limiting are defined in `docs/ai/requirements/feature-api-conventions.md`. Design docs implement these conventions.

## 8. Phased Roadmap

**Phase 1 (MVP)**  
Content schema + CRUD (REST), Authentication (OAuth2), Draft/Publish pipeline (basic, no version history), Multi-language storage (manual input, no AI), GraphQL for public queries, Docker setup.

**Phase 2 (Core AI)**  
AI Translation (Gemini), AI Resume Tailoring (JD analysis + suggestions, apply to main draft; cover letter generation), PDF Generation (OpenPDF), Media Management, SEO Metadata.

**Phase 3 (Polish)**  
Version History with Rollback, Analytics / Visit Tracking, Contact Form / Messaging, Webhooks, Advanced PDF templates, **JD-specific draft variants** (named drafts per JD, publish / promote to main / discard).

## 9. Operations & Observability

- **Health checks:** Spring Boot Actuator; `/actuator/health` (and optionally liveness/readiness for containers). Include MongoDB and filesystem write check where relevant.
- **Logging:** Structured logging (e.g. JSON); log level configurable; no secrets in logs.
- **Docker:** Health check directive in Dockerfile/Compose that hits `/actuator/health`.
- **Metrics (optional):** Counters for publish, PDF generation, AI calls; expose via Actuator if needed.
- Detailed monitoring strategy is in `docs/ai/monitoring/README.md`.

## 10. Non-Goals and Future Scope

- **Out of scope:** Blog/Articles (this is a resume/portfolio site, not a blog); multi-tenant or multi-user CMS; in-app data export/backup (MongoDB backups are sufficient).
- **Future (Phase 4+):** Testimonials/Recommendations may be added later; architecture should not block this.

---

Detailed requirements per feature are in `docs/ai/requirements/`:
- `feature-api-conventions.md` — Error handling, versioning, CORS, rate limiting
- `feature-core-cms.md` — Content schema, CRUD, Auth, Draft/Publish, Version History
- `feature-ai-services.md` — Translation + Resume Tailoring (Gemini)
- `feature-pdf-generation.md` — PDF generation (OpenPDF)
- `feature-extras.md` — Media, SEO, Analytics, Contact, Webhooks

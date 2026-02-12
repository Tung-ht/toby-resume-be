---
phase: requirements
title: Core CMS — Content Schema, CRUD, Auth, Draft/Publish, Version History
description: Content model, REST API, OAuth2, staging pipeline, and version control for Toby.Resume
---

# Core CMS — Content Schema, CRUD, Auth, Draft/Publish, Version History

## Problem Statement
**What problem are we solving?**

- Portfolio content (experience, projects, skills, etc.) must be managed in a structured, multi-language way without hard-coding in the frontend.
- Admin actions must be secured; only the owner (Toby) can create/update/delete content.
- Changes must be staged (draft) and published explicitly, with the ability to roll back to previous versions.

**Who is affected?** Single user (Toby) as content author; visitors consume read-only data via the landing page.

**Current situation:** No existing backend; this defines the backbone of the system.

## Goals & Objectives

- **Primary:** Define a complete content schema, REST CRUD for admin, OAuth2 (Google + GitHub), whole-site draft with publish pipeline, and version history with rollback.
- **Secondary:** Clear separation between draft and published state.
- **Non-goals:** Multi-tenant CMS; WYSIWYG in backend (admin can be form-based); real-time collaboration. **JD-specific drafts** (named draft per JD, publish/promote/discard) are Phase 3 (see feature-ai-services).

## User Stories & Use Cases

- As the **admin**, I want to log in with Google or GitHub so that I can access the admin panel securely.
- As the **admin**, I want to create and edit Hero, Experience, Projects, Education, Skills, Certifications, and Social Links so that my portfolio stays up to date.
- As the **admin**, I want all edits to live in a draft first, with a live preview, so that I don’t break the live site by mistake.
- As the **admin**, I want to publish the draft so that the landing page and PDF reflect the new content, and a version snapshot is saved.
- As the **admin**, I want to roll back to a previous published version so that I can undo a bad publish.
- As a **visitor**, I want the landing page to read only published content via GraphQL so that I always see the latest published state.

**Edge cases:** Empty sections, partial translations. **Publish conflict:** Last-write-wins for MVP; no optimistic locking. If publish is triggered from multiple tabs or devices, the last successful publish wins. Optional conflict detection may be added in Phase 3+.

## Success Criteria

- All content sections are modeled and stored in MongoDB with i18n (locale key per field or per document as designed).
- REST API supports full CRUD for each section; all admin endpoints require valid OAuth2 session.
- GraphQL API exposes only published content; no auth required.
- Publish copies draft → published and creates a version snapshot; rollback restores a past snapshot to published (and optionally to draft).
- **Performance:** REST CRUD p95 latency &lt; 500ms; GraphQL public query p95 &lt; 300ms (typical payload).
- Acceptance: Admin can log in, edit content, preview, publish, and roll back; visitors see only published data.

## Constraints & Assumptions

- **Technical:** Spring Boot 3.x, Java 17+, MongoDB. REST for admin, GraphQL for public only.
- **Business:** Single-user; no billing or quotas.
- **Assumptions:** **Phase 2:** One main draft per site only. **Phase 3:** Multiple JD-specific drafts with flexible workflow (publish directly, promote to main, or discard). Version history uses **full snapshot** per version (simpler rollback; storage acceptable for single-user).

## Content Schema (Data Model)

All entities support **multi-language** via a structure such as `{ "en": { ... }, "vi": { ... } }` or locale-keyed sub-documents. Supported locales: **English (`en`) and Vietnamese (`vi`) only**. No additional languages are in scope.

### Hero / About Me
- `tagline` (string, per locale)
- `bio` (string, Markdown/HTML, per locale)
- `profilePhotoMediaId` (reference to media entity, optional)
- `fullName`, `title` (e.g. "Software Engineer") — optional per locale

### Work Experience
- List of items; each item: `company`, `role`, `startDate`, `endDate` (or "Present"), `bulletPoints` (array of strings per locale), `techUsed` (array of strings), `order` (integer for sort). Locale applies to `company`, `role`, `bulletPoints`.

### Projects / Portfolio
- List of items; each: `title`, `description` (Markdown, per locale), `techStack` (array of strings), `links` (array of `{ label, url }`), `mediaIds` (references to media), `order`, `visible` (boolean).

### Education
- List of items; each: `institution`, `degree`, `field`, `startDate`, `endDate`, `details` (per locale), `order`.

### Skills / Tech Stack
- Categories (e.g. "Languages", "Frameworks"); each category has `name` (per locale) and `items` (array of `{ name, level? }`). Level optional (e.g. "Expert", "Intermediate").

### Certifications & Awards
- List of items; each: `title`, `issuer`, `date`, `url` (optional), `description` (optional, per locale), `order`.

### Social Links
- List of items; each: `platform` (e.g. "GitHub", "LinkedIn"), `url`, `icon` (optional), `order`.

### Global / Settings
- Supported locales (`["en", "vi"]`), default locale.
- **PDF section visibility:** Global configuration specifying which content sections appear in the generated CV/PDF (subset of Hero, Experience, Projects, Education, Skills, Certifications, Social Links). Applies to all PDF exports. Section ordering is global (same order for landing page and PDF). Theming/layout is a frontend concern; the backend does not store theme or layout settings.

**Related entities:** Cover letter content is defined and stored in the AI/JD context (see feature-ai-services).

### Validation Rules (field-level)

| Section | Rule |
|--------|------|
| **Hero** | `tagline` max 500 chars per locale; `bio` max 2000 chars per locale; `profilePhotoMediaId` optional; `fullName`, `title` optional per locale. |
| **Work Experience** | `company`, `role` max 200 chars each; `bulletPoints` max 10 per item, each item max 500 chars; dates ISO or YYYY-MM; `order` integer. |
| **Projects** | `title` max 200 chars; `description` max 3000 chars per locale; `links` max 10 per item; `mediaIds` max 10 per item; `order` integer. |
| **Education** | `institution`, `degree`, `field` max 200 chars each; `details` max 1000 per locale; dates as above; `order` integer. |
| **Skills** | Category `name` max 100 per locale; `items` max 50 per category; item `name` max 100; `level` optional. |
| **Certifications & Awards** | `title`, `issuer` max 200 chars; `description` max 500 per locale; `order` integer. |
| **Social Links** | `platform` max 50; `url` valid URL; `order` integer. |
| **Global** | Supported locales fixed `["en", "vi"]`; default locale must be one of them. |

## Authentication & Authorization

- **OAuth2** with **Google** and **GitHub** as identity providers.
- Session or JWT after OAuth; all REST admin endpoints require authenticated user.
- **Role:** Single role "admin" (single-user); future: could add "viewer" if needed.
- **Public API:** GraphQL has no auth; only published data is exposed.

## Draft & Published Management

- **Draft:** One logical "draft" state per site (or per scope). All section CRUD in admin writes to draft by default. The backend does not store theme/layout; only content is draftable. **Phase 2:** One main draft only. **Phase 3:** Main draft + multiple JD-specific drafts (from AI tailoring).
- **Published:** Read-only copy consumed by GraphQL and PDF generation. Updated only on explicit "Publish" action.
- **Pipeline:** Publish = copy draft → published, create version snapshot, trigger PDF regeneration (and webhooks in Phase 3).
- **Preview API:** The backend must expose a way for the frontend to read **draft** content (e.g. draft GraphQL endpoint or REST endpoint) so that a separate preview URL (e.g. `/preview`) can render the draft version of the site before publish.
- **Concurrent drafts (Phase 3 only):** Main site draft + **multiple** JD-specific drafts. JD drafts can coexist. For each JD draft, the admin can: **publish it directly** (as the live site), **promote it to the main draft** (then publish from there), or **discard** it.

### Initial state & bootstrap

- **First run:** Database is empty; no published content. GraphQL returns empty arrays or null for sections until content exists; landing page handles empty state.
- **Bootstrap:** No mandatory seed data. Admin creates first content via normal CRUD; first publish creates the first version snapshot. Optional: a future seed script may be added for demo.

## Version History

- **Design decision:** Each version stores a **full snapshot** of the published state (simpler rollback; storage cost acceptable for single-user).
- On each **Publish**, store a **version snapshot** (timestamp, optional label).
- **Rollback:** Admin selects a past version; system restores that snapshot to **published** (and optionally replaces current **draft**).
- **Diff/compare:** Optional UI to compare two versions or draft vs published (Phase 3 acceptable).

## Questions & Open Items

- Draft expiration/cleanup policy (e.g. auto-discard draft older than N days).

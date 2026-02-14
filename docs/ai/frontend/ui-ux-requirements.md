---
phase: frontend
title: FrontEnd — UI/UX Requirements
description: User flows, screens, accessibility, i18n, empty states, and error handling for Admin Panel and Landing Page
---

# FrontEnd — UI/UX Requirements

This document defines UI/UX requirements for the **Admin Panel** (React) and **Landing Page** (Next.js) that consume the Toby.Resume backend. It should be used alongside the [API Reference](./api-reference.md) for implementation.

---

## 1. Admin Panel

**Purpose:** Single-user (Toby) content management: edit portfolio sections in draft, preview, and publish. All admin actions require OAuth2 (Google/GitHub) and JWT.

### 1.1 User Roles and Access

| Role | Access |
|------|--------|
| **Admin (Toby)** | Full access after login via Google or GitHub. Only emails/IDs in backend `allowed-admins` can access. |
| **Unauthenticated** | Login page only; redirect to login when accessing any protected route. |

**UX requirement:** If the backend returns `401` or `403`, clear any stored token, show a clear message (“Session expired” or “Access denied”), and redirect to login.

### 1.2 Authentication Flow

1. **Entry:** User opens Admin Panel; if no valid JWT, show **Login** screen.
2. **Login screen:**
   - Two actions: “Log in with Google” and “Log in with GitHub.”
   - Each action navigates the **browser** to the backend URL (same origin or configured API origin):
     - Google: `GET /oauth2/authorization/google`
     - GitHub: `GET /oauth2/authorization/github`
   - Backend redirects to provider; after consent, redirects back to backend callback, then to Admin Panel with JWT (e.g. `{ADMIN_PANEL_URL}/auth/callback?token=<jwt>`).
3. **Callback route (`/auth/callback`):**
   - Read `token` from query (or from cookie if backend sets it).
   - If `error` in query (e.g. `?error=access_denied`), show “Login failed” and link back to login.
   - If token present: store it securely (e.g. memory + httpOnly cookie, or sessionStorage for dev only; avoid localStorage for production if possible). Redirect to dashboard or home.
4. **Logout:** Call `POST /api/v1/auth/logout` (optional; backend may not invalidate token in MVP). Clear stored token and redirect to login.

**UX requirements:**
- Show loading state during OAuth redirect and callback.
- On “Access denied” (not in allowed-admins), show a clear, non-technical message.
- Persist JWT per session; send `Authorization: Bearer <token>` on every REST request.

### 1.3 Navigation and Layout

- **Persistent layout:** Header with app title, primary nav, and user menu (current user name/email, Logout).
- **Primary navigation** (after login) — suggested structure:
  - **Dashboard / Home** — summary: last publish time, “Preview” and “Publish” actions, quick links to sections.
  - **Content sections:** Hero, Experience, Projects, Education, Skills, Certifications, Social Links. Each section has its own list or form view.
  - **Settings** — site settings (locales, default locale, PDF section visibility).
  - **Preview** — open draft preview (new tab or in-app view).
  - **Publish** — trigger publish and show status (can be on Dashboard or dedicated page).

**UX requirement:** Breadcrumbs or clear section labels so the admin always knows which part of the content they are editing.

### 1.4 Content Section UIs

All section data is **draft-only** in the admin. The backend stores two states (DRAFT and PUBLISHED); admin only edits DRAFT.

#### 1.4.1 Hero (Singleton)

- **Screen:** Single form.
- **Fields (per locale):** Tagline, Bio (Markdown/HTML), Full Name, Title. Optional: Profile Photo (media reference; Phase 2).
- **Locales:** Show tabs or toggle for `en` and `vi`. Submit one payload with `Map<locale, value>` (see API Reference).
- **Validation (client-side recommended):** Tagline max 500 chars; bio max 2000; fullName/title max 200 per locale.
- **Actions:** Save (PUT). Show success toast and optionally “View in Preview.”

#### 1.4.2 List Sections (Experience, Projects, Education, Certifications, Social Links)

- **List view:** Table or cards listing items (order, key fields e.g. company/role, title, institution).
- **Actions per item:** Edit, Delete. Add “Add item” button.
- **Reorder:** Drag-and-drop or up/down buttons; on save call `PUT .../reorder` with `orderedIds`.
- **Form (add/edit):** Modal or separate page with all fields. Locale tabs for localized fields. Validation per API (e.g. company/role max 200; bullet points max 10 per locale, each max 500).
- **Projects:** Include “Visible” toggle (visible = shown on public site and in PDF).

**UX requirements:**
- Empty state: “No experience items yet. Add your first one.”
- Delete: Confirm dialog (“Are you sure you want to delete this item?”).
- Inline or toast errors from API: show `error.message` and, if present, `error.details` (field-level) next to the relevant fields.

#### 1.4.3 Skills (Categories + Items)

- **Structure:** Categories (e.g. “Languages”, “Frameworks”); each category has a name (per locale) and a list of items (name, optional level).
- **UI:** List of categories; expand or navigate into a category to edit its name and list of skill items (add/remove/reorder items within category). Category-level reorder via `PUT /api/v1/skills/reorder`.
- **Validation:** Category name max 100 per locale; max 50 items per category; item name max 100.

### 1.5 Preview

- **Entry:** “Preview” button (Dashboard or header). Opens draft as it will appear when published.
- **Implementation:** Call `GET /api/v1/preview?locale=en` (or current locale). Use the same layout/components as the Landing Page where possible, fed with preview payload instead of GraphQL.
- **UX:** Open in new tab or in-app route (e.g. `/preview`) with clear label “Draft preview — not live.” Option to switch locale (en/vi).

### 1.6 Publish

- **Trigger:** “Publish” button (e.g. on Dashboard or dedicated page). Requires confirmation: “Publish draft to live site? This will update the public site and PDF.”
- **Flow:** Call `POST /api/v1/publish`. On success: show success message with `publishedAt` and optionally `versionId`. On 500 (e.g. PUBLISH_FAILED): show error message, suggest retry.
- **Status:** Optionally call `GET /api/v1/publish/status` to show “Last published: …” and version count on Dashboard.

### 1.7 Settings

- **Screen:** Form with:
  - **Supported locales:** Read-only display `["en", "vi"]` (fixed in Phase 1).
  - **Default locale:** Dropdown `en` | `vi`.
  - **PDF section visibility:** Checkboxes for each section (hero, experiences, projects, education, skills, certifications, socialLinks). Determines which sections appear in PDF (backend stores; frontend only needs to send back the same shape).
- **Actions:** Save (PUT). Show validation errors if any (e.g. default locale must be in supported list).

### 1.8 Error Handling (Admin Panel)

| Source | UX requirement |
|--------|-----------------|
| **REST error envelope** | Check `success === false`; display `error.message`. For 400 validation, map `error.details[]` to fields (`field`, `message`). |
| **401** | “Session expired or invalid.” Clear token, redirect to login. |
| **403** | “You don’t have access.” Clear token, redirect to login. |
| **404** | “Item not found.” Navigate back to list or show not-found page. |
| **500 / PUBLISH_FAILED** | “Something went wrong. Please try again.” Option to retry. |
| **Network error** | “Cannot reach server. Check connection.” |

**UX requirement:** Never show raw stack traces or internal error codes to the user. Use `error.code` only for client-side logic (e.g. 401 → logout).

### 1.9 Empty States

- **No hero yet:** “Add your intro: tagline, bio, and title.”
- **No experiences / projects / etc.:** “No items yet. Add your first one.”
- **No published version:** Dashboard can show “You haven’t published yet. Edit content and use Preview, then Publish when ready.”

### 1.10 Accessibility and Responsiveness

- **A11y:** Semantic HTML; labels for form fields; keyboard navigation; focus management in modals; ARIA where needed (e.g. live region for save success/error).
- **Responsiveness:** Admin Panel should be usable on desktop and tablet; forms can stack on small screens.

---

## 2. Landing Page

**Purpose:** Public-facing portfolio/resume. Read-only; data from GraphQL (published content only). No authentication.

### 2.1 Audience and Goals

- **Visitors:** Recruiters, hiring managers, peers.
- **Goal:** Present Toby’s experience, projects, education, skills, certifications, and contact/social links in a clear, professional way. Support English and Vietnamese.

### 2.2 Data Source and Locale

- **API:** `POST /graphql` only. All data from **published** content. No REST admin endpoints.
- **Locale:** Pass `locale: EN` or `locale: VI` in queries. If omitted, backend uses `siteSettings.defaultLocale`. Landing Page should allow switching language (e.g. en/vi toggle) and pass chosen locale to every query.

### 2.3 Page Structure (Suggested)

- **Single-page or multi-page:** Design decision. Single-page with sections (Hero, Experience, Projects, Education, Skills, Certifications, Social Links) is common for resumes.
- **Sections:** One block per content type. Order of sections can match backend order (or a frontend-defined order). Only show sections that have data (or show “Coming soon” for empty sections if desired).

### 2.4 Section Content and Display

| Section | Content | UX notes |
|---------|--------|----------|
| **Hero** | fullName, title, tagline, bio, profilePhotoUrl | Prominent; photo optional. Bio may be Markdown — render with a safe Markdown/HTML renderer. |
| **Experience** | company, role, startDate, endDate, bulletPoints, techUsed | List or timeline. “Present” for null endDate. |
| **Projects** | title, description, techStack, links, mediaUrls | Only `visible: true` items returned by API. Links as buttons or list. |
| **Education** | institution, degree, field, startDate, endDate, details | List or timeline. |
| **Skills** | Categories with name and items (name, level) | Group by category; level optional (e.g. badge or text). |
| **Certifications** | title, issuer, date, url, description | List; link to url if present. |
| **Social Links** | platform, url, icon | Icons or text links (GitHub, LinkedIn, etc.). |

### 2.5 Empty and Loading States

- **Loading:** Skeleton or spinner while GraphQL request is in flight. Prefer one query for full page (see API Reference) to minimize loading phases.
- **Empty section:** If `hero` is null or list is empty, either hide the section or show a neutral “No content” / “Coming soon” so the layout doesn’t break.
- **Full empty (no published content):** Backend returns null/empty for everything. Show a single message: “No content available yet” or maintain layout with placeholders.

### 2.6 Errors (Landing Page)

- **GraphQL errors:** Check `errors` array in response. Show a generic “Unable to load content. Please try again later.” Do not expose internal messages or stack traces.
- **Network error:** “Cannot load content. Check your connection and refresh.”

### 2.7 SEO and Performance (Future)

- **Phase 1:** Basic meta tags (title, description) can be set from a default or from a future SEO API. Not required for MVP.
- **Performance:** Prefer one consolidated GraphQL query for the whole page; cache response as appropriate (e.g. ISR in Next.js).

### 2.8 Accessibility and Responsiveness

- **A11y:** Semantic sections (e.g. `<section>`, headings hierarchy); alt text for images; focus order; sufficient contrast.
- **Responsiveness:** Readable on mobile and desktop; touch-friendly links and buttons.

### 2.9 Language Switching

- **UI:** Language switcher (e.g. “EN | VI”) that sets the locale for GraphQL and re-fetches or updates content. Store preference in URL (e.g. path or query) or localStorage so it persists across visits.

---

## 3. Shared UX Conventions

### 3.1 Locales

- **Supported:** `en` (English), `vi` (Vietnamese) only.
- **Display names:** “English” and “Tiếng Việt” (or “Vietnamese”) in language switchers and settings.

### 3.2 Date Display

- Backend stores dates as `YYYY-MM` or full ISO where applicable. Frontend may format for display (e.g. “Jan 2023”, “Present” for null endDate).

### 3.3 Markdown and Links

- Hero bio and project descriptions may contain Markdown. Use a safe Markdown renderer; sanitize HTML to prevent XSS.
- Links (project links, certifications, social links) must open in a new tab with `rel="noopener noreferrer"` where appropriate.

### 3.4 Theming and Branding

- Theming/layout are **frontend-owned**. Backend does not store theme or layout; only content and PDF section visibility. Admin Panel and Landing Page can have distinct visual designs.

---

## 4. Out of Scope (Phase 1)

- **Media upload:** No media upload in Phase 1; profile photo and project media are optional references (can be placeholder or omitted).
- **PDF generation:** Backend PDF generation is Phase 2; frontend does not trigger PDF in Phase 1.
- **Contact form:** Phase 3; no contact form on Landing Page in Phase 1.
- **Version history / rollback UI:** Phase 3; admin does not see list of versions or rollback in Phase 1.
- **AI translation / tailoring:** Phase 2+.

---

## 5. Summary Checklist (Implementers)

**Admin Panel**

- [ ] Login with Google and GitHub; handle callback and token storage.
- [ ] Send `Authorization: Bearer <token>` on all `/api/v1/*` requests.
- [ ] Dashboard with Preview and Publish; links to all sections.
- [ ] CRUD UIs for Hero, Experience, Projects, Education, Skills, Certifications, Social Links (with reorder where applicable).
- [ ] Settings: default locale, PDF section visibility.
- [ ] Preview: `GET /api/v1/preview?locale=…` and render with same structure as Landing Page.
- [ ] Publish: confirm and `POST /api/v1/publish`; show success/error.
- [ ] Handle 401/403 by clearing token and redirecting to login.
- [ ] Show validation errors from `error.details` on forms.
- [ ] Empty states and loading states.

**Landing Page**

- [ ] Single GraphQL endpoint; one main query with locale variable.
- [ ] Render Hero, Experience, Projects, Education, Skills, Certifications, Social Links from published data.
- [ ] Language switcher (en/vi); pass locale to GraphQL.
- [ ] Empty and loading states; generic error message on failure.
- [ ] Responsive and accessible layout.

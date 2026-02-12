---
phase: requirements
title: Extras — Media, SEO, Analytics, Contact Form, Webhooks
description: Media management, SEO metadata, visit tracking, contact form, and publish webhooks
---

# Extras — Media, SEO, Analytics, Contact Form, Webhooks

## Problem Statement
**What problem are we solving?**

- Portfolio content needs images (profile photo, project screenshots); these must be uploaded, stored, and referenced in content.
- The landing page needs to be discoverable (SEO) and its usage measurable (analytics); visitors need a way to get in touch (contact form).
- External systems (e.g. frontend rebuild, notifications) need to be notified when content is published (webhooks).

**Who is affected?** Toby (media, SEO, analytics dashboard, messages); visitors (discovery, contact); integrators (webhooks).

## Goals & Objectives

- **Primary:** Media upload and management (local storage), SEO metadata (meta tags, Open Graph, JSON-LD, sitemap), basic analytics (page views, referrers), contact form with storage and notification, and configurable webhooks on publish.
- **Secondary:** Thumbnails for images; optional Google Analytics hook; spam protection for contact form; webhook retry policy.
- **Non-goals:** CDN or cloud media in v1 (local only); full-featured analytics (no heatmaps/session replay). Contact notification uses **SMTP** (generic config; Gmail SMTP recommended for simplicity).

## User Stories & Use Cases

- As the **admin**, I want to upload images (profile photo, project images) so that I can use them in Hero and Projects.
- As the **admin**, I want to set meta title, description, and Open Graph image per page (or global) so that shares and search results look correct.
- As the **admin**, I want to see basic visit stats (page views, referrers) so that I understand traffic.
- As a **visitor**, I want to submit a message via a contact form so that Toby can get in touch.
- As the **admin** or **integrator**, I want to configure a webhook URL that is called when I publish so that I can trigger a rebuild or send a notification.

**Edge cases:** Large uploads (size limits); invalid image formats; contact form spam; webhook endpoint down (retry, then mark failed).

## Success Criteria

- Media: Upload via REST; store in local filesystem (Docker volume); reference by id in content; list/delete in admin. Validation: size and format limits.
- SEO: Store and serve meta title, description, keywords; Open Graph tags; JSON-LD Person schema; sitemap endpoint or file.
- Analytics: Record page view (path, referrer, timestamp); dashboard to view counts and top referrers; optional GA id for frontend.
- Contact: Public form submits to REST at **`POST /api/public/contact`** (canonical path; consistent with other public endpoints). Message stored in DB; **one-way only** (visitor sends, admin reads; no reply-from-admin). **Email notification** to admin on new message via **SMTP** (generic config). Rate limiting and honeypot.
- Webhooks: **General-purpose** configurable URL(s); POST on publish with payload (e.g. timestamp, version id); retry with backoff; admin can view log of deliveries. Use cases: frontend rebuild, Telegram/Discord, CI/CD, or any custom integration.
- **Performance:** Contact form submit response &lt; 2s (sync save + async email). Webhook delivery is async with retry; publish does not block on webhook success.

## Constraints & Assumptions

- **Technical:** Local filesystem for media (no S3 in v1); MongoDB for messages and analytics events (or dedicated store if volume is high). Webhooks are best-effort; at-least-once delivery with retries.
- **Assumptions:** Single admin; no multi-tenant media namespacing. Contact form does not require sign-in. SEO is per-site or per-page as designed.

---

## Media Management

- **Upload:** REST endpoint (multipart); accept images (e.g. JPEG, PNG, WebP). Max file size (e.g. 5MB); store under configured path (Docker volume). Return media id and URL (or path for frontend to resolve).
- **Storage:** Local filesystem; path structure e.g. `media/{year}/{month}/{id}.{ext}` or `media/{id}.{ext}`. Media document in MongoDB: id, filename, mimeType, size, createdAt, usedIn (optional list of content refs).
- **Validation:** File type allowlist; size limit. Reject invalid uploads with 400.
- **Thumbnails:** Optional: generate thumbnail on upload (e.g. for gallery); store alongside original or in subfolder.
- **Library:** List media; delete by id (soft or hard; if hard, references in content may break unless validated).
- **Usage:** Content entities (Hero, Projects) reference media by `mediaId`; resolver or API serves file by id.

## SEO Metadata

- **Data model:** Global or per-page: `metaTitle`, `metaDescription`, `metaKeywords`, `ogImageMediaId` (or URL), optional `canonicalUrl`. Stored in settings or page collection; draft/published if needed.
- **Serving:** GraphQL or REST exposes these fields so the landing page can render `<meta>`, `<meta property="og:...">`, and JSON-LD script.
- **JSON-LD:** Person schema (name, jobTitle, url, image, sameAs for social). Generated from Hero + Social Links or stored explicitly.
- **Sitemap:** Endpoint (e.g. `GET /sitemap.xml`) that returns XML sitemap with landing page URL(s) and optional lastmod. Static file or generated on request.

## Analytics / Visit Tracking

- **Scope:** **Basic** only: page views, referrers, rough visitor counts, dashboard with charts. No PDF download tracking; no heatmaps or session replay.
- **Event:** On each public page view (or API that frontend calls), record: path (or page id), referrer, userAgent (optional), timestamp, optional session/id cookie to approximate unique visitors.
- **Storage:** MongoDB collection (e.g. `page_views`); optional TTL index to auto-delete old data (e.g. 1 year). Or aggregate daily/hourly for dashboard and drop raw events after aggregation.
- **Dashboard:** REST (admin): aggregate by path, by referrer, time range; return counts and top N. No PII; anonymized.
- **Google Analytics:** Optional: store GA measurement id in settings; frontend injects GA script; backend does not send to GA (frontend-only). Or backend can proxy events if required.

## Contact Form / Messaging

- **Public endpoint:** **`POST /api/public/contact`** with body: name, email, subject, message. No auth required. Same path prefix as other public endpoints (e.g. CV download).
- **Validation:** Required fields; max length; sanitize input. Honeypot field (hidden) to catch bots; rate limit by IP (e.g. 5 per hour).
- **Storage:** Save to MongoDB (e.g. `contact_messages`: name, email, subject, message, createdAt, ipHash or ip, read flag). Admin can list and mark as read.
- **One-way flow:** Visitor sends message only. Admin reads in panel; reply is done externally (e.g. via own email). No in-app reply feature.
- **Notification:** On new message, send **email** to admin via **SMTP**. Use generic SMTP configuration (e.g. host, port, user, password); **Gmail SMTP** is recommended for simplicity. Optional: CAPTCHA (e.g. reCAPTCHA) in Phase 3.
- **Spam:** Rate limiting, honeypot; optional CAPTCHA later.

## Webhooks

- **Config:** Store webhook URL(s) in settings (admin only). Payload: e.g. `{ "event": "published", "versionId": "...", "timestamp": "..." }`. Optional: secret for HMAC signature.
- **Trigger:** On successful publish, call each configured URL (POST). Async with retry (e.g. 3 retries, exponential backoff). Log success/failure per webhook for admin.
- **Scope:** **General-purpose** — not limited to a specific use case. Typical uses: trigger frontend rebuild, notify Telegram/Discord, trigger CI/CD, or any custom integration.
- **Security:** Validate HTTPS in config; optional secret so receiver can verify request.

## Questions & Open Items

- Media: thumbnail size and format (e.g. 200px max dimension); whether to support non-image files (e.g. PDF) later.
- Analytics: retention period (e.g. 1 year with TTL index); GDPR considerations (anonymize IP, no cookies if possible).
- Contact: whether to send confirmation email to sender (would require email config; Phase 3 or later).
- Webhook: max URLs (e.g. 5); include payload schema version in webhook body for forward compatibility.

---
phase: requirements
title: PDF Generation — iText, Templates, Localized Exports, Caching
description: CV export to PDF using iText/OpenPDF with templates and language support
---

# PDF Generation — iText, Templates, Localized Exports, Caching

## Problem Statement
**What problem are we solving?**

- Visitors and the owner need a downloadable, professional CV in PDF form that matches the portfolio content and language.
- PDFs must be generated reliably without depending on a headless browser (simplicity, Docker-friendly, Java-native).

**Who is affected?** Toby (download for applications); visitors (download from landing page). Both need correct language and up-to-date content.

**Current situation:** No PDF generation; requirement originally mentioned "web-to-PDF" but stack choice is pure Java (iText/OpenPDF).

## Goals & Objectives

- **Primary:** Generate PDFs from **structured content** (published state) using **OpenPDF** (pure Java). Support **localized exports** (e.g. English PDF when viewing EN; Vietnamese PDF when viewing VI). Support **templates** (different CV layouts/styles).
- **Secondary:** **Caching** — regenerate only when content or locale or template changes to avoid redundant work.
- **Non-goals:** WYSIWYG PDF editor in backend; real-time collaborative editing of PDF; non-LTR layouts in v1 (EN/VI are LTR).

## User Stories & Use Cases

- As the **admin**, I want to trigger PDF generation after publish so that the latest published content is available as PDF.
- As the **admin** or **visitor**, I want to download the CV in my chosen language so that I get an English or Vietnamese PDF accordingly.
- As the **admin**, I want to choose among available PDF templates (e.g. "Modern", "Classic") so that the look can match my brand.
- **System:** When published content or template changes, cached PDFs for affected locale are invalidated and regenerated on next request.

**Edge cases:** Very long content (pagination); missing optional sections; missing profile photo (placeholder or omit).

## Success Criteria

- PDF is generated using **OpenPDF** from published content + selected template + locale.
- At least one default template; optional second template to prove extensibility.
- Caching: per (locale, template) or per (content hash, locale, template); regenerate only when content/template/locale changes.
- Localized exports: PDF text follows the requested locale (same as landing page language switching).
- Layout: LTR for EN/VI; no RTL in v1.
- **Performance:** PDF generation completes in &lt; 10s for a typical CV; cache hit (serving stored PDF) &lt; 200ms.

## Constraints & Assumptions

- **Technical:** Spring Boot 3.x, Java 17+; **OpenPDF** (LGPL; no license risk for v1). No headless browser. Storage of generated PDFs: local filesystem (Docker volume) or in-memory cache with file fallback.
- **Assumptions:** PDF is read-only output; no form filling or digital signatures in v1.

## PDF Engine

- **Library:** **OpenPDF** (pure Java, LGPL). Chosen for v1 to avoid license risk; no commercial license required.
- **Input:** Published content in the requested locale, plus template id and optional options (e.g. font size). Which sections are included is determined by the **global PDF section visibility** setting (see feature-core-cms); only sections enabled for PDF export are rendered.
- **Output:** PDF bytes; served as download (e.g. `Content-Disposition: attachment`) or stored for later retrieval. Filename can include locale and date (e.g. `cv-en-2026-02-11.pdf`).

## Templates

- **Default template:** One built-in template (e.g. "Default" or "Modern") that renders all sections in a clean layout.
- **Template management:** Ability to register additional templates (e.g. "Classic"); admin can select default template per site or per request. **Recommendation:** Code-first templates (Java) for v1; config-driven (e.g. JSON/YAML) can be added later if needed.
- **Sections in template:** Which sections appear in the PDF is controlled by the **global PDF section visibility** config (feature-core-cms). Templates render only those sections; section order is global. Per-template show/hide beyond that is out of scope for v1.

## Localized Exports

- **Locale parameter:** API accepts `locale` (e.g. `en`, `vi`). Content passed to the PDF engine is the published content for that locale.
- **RTL:** Not required for EN/VI; layout is LTR only in v1.
- **Fonts:** Support for Latin and Vietnamese characters; embed or use system fonts as needed.

## Caching

- **Cache key:** Combination of (published content version or hash, locale, template id). When publish happens, invalidate all cached PDFs for that content (or all locales/templates for that content).
- **Storage:** Local filesystem under Docker volume; or in-memory with TTL. Regenerate on cache miss or invalidation.
- **Optional:** Pre-generate PDFs on publish for default locale(s) to make first download fast.

## API

- **REST (admin):** Trigger regenerate PDF for locale/template; optional "regenerate all" after publish.
- **Public download (unauthenticated):** Visitors must be able to download the CV PDF from the landing page. Backend exposes a **public download endpoint** (e.g. `GET /api/public/cv.pdf?locale=en` or GraphQL query returning a download URL). No auth required; used by the landing page for the public "Download CV" button.
- **Publish pipeline:** On publish, invalidate cache and optionally trigger async regeneration for default locale/template.

## Questions & Open Items

- Max PDF size / pagination for very long content (design phase).
- Whether to store last-generated PDF path per locale in DB for audit (optional).

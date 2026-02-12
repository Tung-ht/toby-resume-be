---
phase: requirements
title: AI Services — Translation & Resume Tailoring (Gemini)
description: Google Gemini integration for auto-translation and JD-based resume tailoring
---

# AI Services — Translation & Resume Tailoring (Gemini)

## Problem Statement
**What problem are we solving?**

- Manually translating portfolio content (EN ↔ VI) is time-consuming and error-prone; formatting (Markdown/HTML) must be preserved.
- Tailoring a resume for each job application is tedious; matching JD keywords and reordering/rewriting content should be assisted by AI.

**Who is affected?** Toby as content author; visitors benefit from accurate translations and (indirectly) from better-tailored applications.

**Current situation:** No AI integration; content is edited manually only.

## Goals & Objectives

- **Primary:** Integrate **Google Gemini** for (1) AI Auto-Translate between supported locales (EN/VI only), (2) AI Resume Tailoring: JD analysis, suggestions (reorder, rewrite, and **suggest new content** such as bullet points or skills to add), one-click draft creation, and (3) **AI Cover Letter** generation per JD.
- **Secondary:** Store JDs for reference; maintain tailoring history; support manual override of AI outputs. **Usage tracking:** track tokens, API calls, and cost estimate (no hard limits); display stats in admin.
- **Non-goals:** Fully automated apply-without-review; support for AI providers other than Gemini in v1 (abstraction can be added later).

## User Stories & Use Cases

- As the **admin**, I want to click "AI Translate" on a section or full site so that content is translated into the target language while preserving Markdown/HTML.
- As the **admin**, I want to paste a Job Description and get AI suggestions (project order, bullet rewrites) so that I can align my resume with the role.
- As the **admin**, I want to "Apply" suggestions to the main draft (Phase 2) so that I can review and publish. In Phase 3, I want to create a **named draft variant** per JD and choose to publish, promote to main, or discard.
- As the **admin**, I want to save past JDs and see tailoring history so that I can reuse or compare versions.
- As the **admin**, I want the AI to generate a cover letter for a JD so that I can review and copy/paste it into applications.
- As the **admin**, I want to see AI usage stats (tokens, calls, cost estimate) so that I can monitor spending.

**Edge cases:** Very long JD/content (token limits); translation failure (show error, keep source); empty or irrelevant JD (graceful degradation).

## Success Criteria

- Translation: Given source locale and target locale, Gemini returns translated text preserving structure; backend stores it in draft; user can edit after.
- Tailoring: Given JD + current (draft or published) profile, Gemini returns structured suggestions (e.g. suggested order of projects, suggested bullet rewrites); backend applies suggestions to main draft (Phase 2) or creates a named draft variant (Phase 3).
- **Phase 2:** Apply suggestions updates the **main draft** only. **Phase 3:** One-click apply creates a **named draft** (e.g. "Draft for Company X") that user can edit, **publish directly**, **promote to main draft**, or **discard**; multiple JD-specific drafts can coexist.
- JDs are stored with optional label (company name, date); tailoring history links drafts to JDs (Phase 3).
- Cover letter: AI generates editable text per JD; stored in admin (see Data model below); no PDF output (admin copies/pastes as needed).
- Usage tracking: backend records tokens/calls per request; admin can view aggregate stats and cost estimate (no enforced limits).
- **Performance:** Translation request timeout 60s; tailoring request timeout 90s. On timeout or API failure, return error and do not apply partial changes.

## Constraints & Assumptions

- **Technical:** Google Gemini API (key in env); token limits and rate limits apply; prompts must be designed for clarity and safety.
- **Assumptions:** Gemini is available and acceptable for production; cost is monitored. Fallback on API failure: return error, no partial apply.

## AI Auto-Translate

- **Trigger:** Admin selects source content (section or full site) and target language (**en** or **vi**); clicks "AI Translate."
- **Full-site translate:** Chunked by section (or by token budget) to respect Gemini limits; a single "Translate full site" action from the UI triggers multiple backend calls. Backend orchestrates and writes all results to draft.
- **Input:** Structured content (e.g. JSON or per-field) with locale tags; Markdown/HTML segments identified.
- **Process:** Backend sends segments to Gemini with system prompt: translate, preserve Markdown/HTML and structure, preserve placeholders/codes. Target locale (`en` or `vi`) specified.
- **Output:** Translated content written to **draft** for the target locale; source locale unchanged.
- **Fallback:** On API error or timeout, return error message; do not overwrite existing target content. User can retry or edit manually.
- **Manual override:** User can always edit any AI-translated text in the admin panel.

## AI Resume Tailoring

- **Input:** Pasted JD (plain text or HTML); optional label (e.g. company name). Stored as a **Job Description** entity (id, label, rawText, createdAt).
- **AI Suggestion (Gemini):**
  - Analyze JD for key terms, required skills, tech stack, emphasis.
  - Compare with current profile (projects, experience bullets, skills).
  - Output structured suggestions, e.g.:
    - **Project order:** Recommended order of projects (by relevance to JD).
    - **Bullet rewrites:** For each experience/project bullet, optional suggested rewrite aligning with JD language.
    - **Suggested additions:** Optional new bullet points, skills to add, or other content recommendations for the user to review and accept or edit.
  - Scope may extend to: skills to emphasize, bio/tagline tone (configurable in prompt or settings).
- **Phase 2 — Apply to main draft:** Backend applies suggestions to the **main draft** (no named JD drafts). User reviews, edits, and publishes as usual.
- **Phase 3 — One-click Apply (named draft):** Backend generates a **new draft variant** (copy of current draft or published, then apply suggestions). Draft is named/linked to the JD (e.g. "Draft for Company X"). **Multiple JD drafts can coexist.** User can then **publish it directly**, **promote it to the main draft**, **edit further**, or **discard**.
- **JD Storage:** Persist JDs with optional label and date; list in admin for re-use. Re-running tailoring on same JD can create another draft or update existing (product decision).
- **Tailoring history (Phase 3):** Link drafts to JD id; show "Created from JD: Company X" in admin.

## Prompt Engineering & Model

- **Model:** **Google Gemini.** Default: `gemini-1.5-flash` for translation and high-volume calls (cost-effective). Config override to `gemini-1.5-pro` for tailoring and cover letter allowed if quality is preferred. Model name configurable (e.g. env or settings).
- **Prompts:** Separate prompts for (1) translation (with structure preservation rules) and (2) tailoring (with output schema for order + rewrites). Prompts must be versioned and configurable (e.g. in config or DB) for iteration.
- **Structured output:** Prefer JSON schema for tailoring output so backend can parse and apply without free-form parsing.

## AI Cover Letter Generation

- **Trigger:** When user has a JD (saved or pasted), they can request a **cover letter** for that JD.
- **Process:** Backend sends JD + relevant profile summary (e.g. role, key experience) to Gemini; prompt asks for a tailored cover letter.
- **Output:** **Editable text** only. No PDF generation for cover letter; admin copies/pastes into applications as needed.
- **Phase:** Same as AI Resume Tailoring (Phase 2).

### Data model (Cover Letter)

- **Storage:** Store cover letter text linked to the JD. Option A: field on Job Description entity, e.g. `JobDescription.coverLetterText` (string). Option B: separate document/entity with `jobDescriptionId` and `content`. Choose in design phase.
- **Fields:** At minimum: content (editable text), optional `updatedAt`. Admin can overwrite after AI generation.

## AI Usage Tracking

- **Scope:** Track Gemini API usage: token count, request count, and (if available) cost estimate per request or per day/month.
- **Storage:** Store aggregated or per-call metrics; expose via REST (admin) for dashboard display.
- **Behavior:** **No hard limits** — do not block or throttle based on usage. Display stats so the admin can monitor spending externally.
- **Non-goals:** Enforcing daily/monthly caps; multi-tenant quotas.

## Questions & Open Items

- Token limits for very long JDs (chunking or truncation strategy if needed).

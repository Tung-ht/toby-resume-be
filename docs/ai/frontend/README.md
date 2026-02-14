---
phase: frontend
title: FrontEnd Application — Documentation Index
description: UI/UX requirements and API reference for building the Toby.Resume Admin Panel and Landing Page
---

# FrontEnd Application Documentation

This folder contains documentation for building the **FrontEnd applications** that consume the Toby.Resume backend:

| Application | Tech (per design) | Purpose | Primary API |
|-------------|--------------------|---------|-------------|
| **Admin Panel** | React (separate repo) | Content management, auth, draft preview, publish | REST `/api/v1/*` + OAuth2 |
| **Landing Page** | Next.js (separate repo) | Public portfolio/resume view for visitors | GraphQL `POST /graphql` |

## Document Map

| Document | Description |
|----------|-------------|
| [UI/UX Requirements](./ui-ux-requirements.md) | User flows, screens, accessibility, i18n, empty states, error handling, and design constraints for both applications. |
| [API Reference](./api-reference.md) | Complete REST and GraphQL API reference for frontend developers: endpoints, request/response shapes, auth, errors, and examples. |

## Backend Context

- **Design:** `docs/ai/design/phase1-mvp.md`, `docs/ai/design/api-design.md`
- **Requirements:** `docs/ai/requirements/feature-core-cms.md`, `docs/ai/requirements/feature-api-conventions.md`
- **REST base URL:** `http://localhost:8080` (dev) or configured API host
- **REST prefix:** `/api/v1/`
- **GraphQL endpoint:** `POST /graphql`
- **Locales:** English (`en`) and Vietnamese (`vi`) only.

## Quick Links for Implementers

- **Admin Panel:** Use [UI/UX Requirements — Admin Panel](./ui-ux-requirements.md#1-admin-panel) and [API Reference — REST](./api-reference.md#2-rest-api-admin-panel).
- **Landing Page:** Use [UI/UX Requirements — Landing Page](./ui-ux-requirements.md#2-landing-page) and [API Reference — GraphQL](./api-reference.md#3-graphql-api-landing-page).

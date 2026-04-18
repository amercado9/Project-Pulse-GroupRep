# Project Pulse – Claude Code Guide

## Project Overview

Project Pulse is a full-stack web application built from structured requirements located in `/requirements`.

The goal is to implement the system faithfully, using a clean architecture, consistent coding standards, and a team-based workflow.

---

## Source of Truth (READ FIRST)

Before making design or implementation decisions, always review:

1. `/requirements/vision_scope.md`
2. `/requirements/use_cases.md`
3. `/requirements/project_glossary.md`

Do not invent features not supported by requirements unless clearly marked as enhancements.

---

## Required Architecture

### Backend (Spring Boot)

Use domain-oriented modules with internal layers.

Each module should contain:

- controller
- service
- repository
- domain
- dto

Rules:

- Organize by business/domain first
- Keep business logic out of controllers
- Keep persistence logic inside repositories
- Minimize cross-module coupling
- Do not introduce global layer folders (e.g., no top-level controller/)

### Frontend (Vue 3)

Organize by feature/domain.

Rules:

- Keep API logic in services
- Keep UI logic in components
- Keep feature code grouped together
- Extract shared code only when necessary

---

## Tech Stack

- Frontend: Vue 3 + Vite
- Backend: Spring Boot
- Database: MySQL
- CI/CD: GitHub Actions
- Deployment: Microsoft Azure

See `/docs/tech-stack.md` for details.

---

## Implementation Rules

- Follow coding standards in `/docs/coding-standards.md`
- Follow API conventions in `/docs/api-guidelines.md`
- Keep commits small and focused
- Write tests for core logic
- Maintain requirement traceability

---

## Team Workflow

- One use case per branch
- No direct commits to `main`
- PR required for all merges
- Update `/docs/development-plan.md` when tasks change

See `/docs/team-workflow.md`

---

## Supporting Docs

- Architecture: `/docs/architecture.md`
- Development Plan: `/docs/development-plan.md`
- Coding Standards: `/docs/coding-standards.md`
- Decisions (ADRs): `/docs/decisions/`
- Deployment: `/docs/deployment.md`

---

## Before Writing Code

Always:

1. Identify the relevant use case
2. Confirm requirements alignment
3. Check architecture rules
4. Follow coding standards
5. Ensure consistency with existing code

---

## When Unsure

- Do not guess — ask for clarification
- Do not introduce new patterns without justification
- Check `/docs/decisions/` before changing architecture

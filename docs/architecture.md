# Architecture

## Overview

Project Pulse uses a domain-oriented modular architecture.

The backend is organized by business/domain modules first, and each module is internally layered. This improves maintainability, reduces coupling, and allows parallel development.

The frontend follows a similar principle: organize by feature/domain while keeping shared code separate.

---

## Architectural Style

### Backend

We use domain-oriented modules with internal layers.

Each module contains:

- controller
- service
- repository
- domain
- dto

Benefits:

- Strong separation of concerns
- Clear ownership per domain
- Easier scaling
- Reduced merge conflicts in team development

### Frontend

The frontend is organized by feature/domain:

- feature-specific code grouped together
- shared UI and utilities extracted into common folders

---

## Backend Structure

Example:

backend/
└── src/main/java/com/tcu/projectpulse/
├── config/
├── shared/
├── auth/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ ├── domain/
│ └── dto/
├── user/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ ├── domain/
│ └── dto/
├── project/
│ ├── controller/
│ ├── service/
│ ├── repository/
│ ├── domain/
│ └── dto/
└── requirement/
├── controller/
├── service/
├── repository/
├── domain/
└── dto/

---

## Layer Responsibilities

### controller

- Handles HTTP requests and responses
- Validates request structure
- Calls service layer
- Returns response

No business logic here.

---

### service

- Implements use cases
- Contains business logic
- Coordinates repositories and other services

---

### repository

- Handles database access
- Uses Spring Data JPA
- Contains query methods only

No business logic.

---

### domain

- Core domain models
- Entities
- Value objects
- Enums

May include domain-specific logic.

---

### dto

- Request and response objects
- Used for API communication

Should not be used as persistence entities.

---

## Shared Code

Use shared/ only for:

- common exceptions
- utility classes
- response wrappers
- shared validation

Avoid moving code here too early.

---

## Dependency Rules

- Controller → Service
- Service → Repository + Domain
- Repository → Domain

Rules:

- No business logic in controllers
- No persistence logic in services
- Domain should not depend on controllers
- Minimize cross-module dependencies

---

## API Flow

Client → Controller → Service → Repository → Database

---

## Frontend Structure

frontend/
└── src/
├── router/
├── layouts/
├── shared/
│ ├── components/
│ ├── services/
│ ├── utils/
│ └── types/
├── features/
│ ├── auth/
│ ├── projects/
│ └── requirements/

---

## Key Principles

- Organize by domain first, then layer
- Keep business logic in services/domain
- Keep persistence isolated in repositories
- Avoid tight coupling between modules
- Prefer clear ownership per module

---

## Team Guidance

- Assign work by domain, not random use cases
- Each module should have a primary owner
- Cross-module changes require coordination
- Do not break module boundaries for convenience

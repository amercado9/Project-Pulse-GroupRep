# Project Pulse

A web application for managing weekly activity reports (WAR) and peer evaluations in TCU's senior design courses.

## Problem

The current process for submitting and grading WARs and peer evaluations relies on Google Sheets, Excel spreadsheets, and manual file transfers through the university's LMS. This workflow is time-consuming, error-prone, and creates a heavy manual burden for both students and instructors.

## Solution

Project Pulse consolidates the entire workflow into a single web application where:

- **Students** submit weekly activity reports and peer evaluations directly in the system
- **Instructors** view reports, review peer evaluations, and access automatically generated grades and feedback
- **Admins** manage senior design sections, teams, and student/instructor assignments

## Key Features

- Manage senior design sections, teams, students, and instructors
- Submit and track weekly activity reports
- Submit peer evaluations with rubric-based scoring
- Generate peer evaluation and WAR reports automatically

## Tech Stack

- **Frontend:** Vue 3 + Vite
- **Backend:** Spring Boot (Java 17+)
- **Database:** MySQL
- **CI/CD:** GitHub Actions
- **Deployment:** Microsoft Azure

## Project Structure

```
project-pulse/
├── backend/          # Spring Boot application
├── frontend/         # Vue 3 application
├── requirements/     # Vision & scope, use cases, glossary
├── docs/             # Architecture, coding standards, API guidelines, etc.
└── .github/          # CI/CD workflows
```

## Documentation

- [Architecture](docs/architecture.md)
- [Tech Stack](docs/tech-stack.md)
- [API Guidelines](docs/api-guidelines.md)
- [Coding Standards](docs/coding-standards.md)
- [Development Plan](docs/development-plan.md)
- [Team Workflow](docs/team-workflow.md)
- [Testing Strategy](docs/testing-strategy.md)
- [Deployment](docs/deployment.md)

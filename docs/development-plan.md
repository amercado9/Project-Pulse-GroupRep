# Development Plan

## Phase 1 – Foundation

- Initialize frontend and backend
- Setup database
- Setup CI/CD
- Establish domain module structure
- Define coding standards

---

## Phase 2 – Core Domains

Domains:

- Auth / Section / Rubric (Alexa)
- User (Delia)
- Team / Activity / Evaluation (Emmanuel)

---

## Team Ownership

### Alexa — Auth, Section, Rubric

Modules: `auth`, `section`, `rubric`

| UC | Description | Status |
|----|-------------|--------|
| UC-1 | Create a rubric | Not started |
| UC-2 | Find senior design sections | Not started |
| UC-3 | View a senior design section | Not started |
| UC-4 | Create a senior design section | Not started |
| UC-5 | Edit a senior design section | Not started |
| UC-6 | Set up active weeks for a section | Not started |
| UC-25 | Student sets up a student account | Not started |
| UC-31 | Generate peer eval report for entire section | Not started |
| UC-32 | Generate WAR report for a team | Not started |
| UC-34 | Generate WAR report for a student | Not started |

---

### Delia — User Management

Modules: `user`

| UC | Description | Status |
|----|-------------|--------|
| UC-11 | Invite students to join a senior design section | Not started |
| UC-15 | Find students | Not started |
| UC-16 | View a student | Not started |
| UC-17 | Delete a student | Not started |
| UC-18 | Admin creates an instructor account directly | Not started |
| UC-21 | Find instructors | Not started |
| UC-22 | View an instructor | Not started |
| UC-23 | Deactivate an instructor | Not started |
| UC-24 | Reactivate an instructor | Not started |
| UC-26 | Student edits account | Not started |

---

### Emmanuel — Team, Activity, Evaluation

Modules: `team`, `activity`, `evaluation`

| UC | Description | Status |
|----|-------------|--------|
| UC-7 | Find senior design teams | Not started |
| UC-8 | View a senior design team | Not started |
| UC-9 | Create a senior design team | Not started |
| UC-10 | Edit a senior design team | Not started |
| UC-12 | Assign students to senior design teams | Not started |
| UC-13 | Remove a student from a senior design team | Not started |
| UC-14 | Delete a senior design team | Not started |
| UC-28 | Submit peer evaluation for the previous week | Not started |
| UC-29 | View own peer evaluation report | Not started |
| UC-33 | Generate peer eval report for a student | Not started |

---

### Shared — All Three

| UC | Description | Primary Owner | Status |
|----|-------------|---------------|--------|
| UC-27 | Student manages activities in a WAR | Emmanuel (lead) | Not started |

---

## Removed Use Cases

| UC | Reason |
|----|--------|
| UC-30 | Depended on UC-18 invite email. Replaced by UC-18 (simplified). Instructors receive credentials directly from Admin. |

---

## Phase 3 – Use Case Implementation

Within each domain:

1. Implement core entities
2. Implement repository layer
3. Implement service logic
4. Implement controller endpoints
5. Implement frontend views

---

## Shared Responsibilities

- CI/CD
- Deployment
- Database schema
- API consistency

---

## Definition of Done

- Meets requirement specification
- Follows architecture rules
- Code reviewed
- Tested
- Integrated successfully

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
| UC-1 | Create a rubric | Done |
| UC-2 | Find senior design sections | Done |
| UC-3 | View a senior design section | Done |
| UC-4 | Create a senior design section | Done |
| UC-5 | Edit a senior design section | Done |
| UC-6 | Set up active weeks for a section | Done |
| UC-25 | Student sets up a student account | Done |
| UC-31 | Generate peer eval report for entire section | Not started |
| UC-32 | Generate WAR report for a team | Done |
| UC-33 | Generate peer eval report for a student | Not started |
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
| UC-18 | Admin generates instructor invite link (sends manually) | Not started |
| UC-30 | Instructor sets up account via invite link | Not started |
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
| UC-7 | Find senior design teams | Done |
| UC-8 | View a senior design team | Done |
| UC-9 | Create a senior design team | Done |
| UC-10 | Edit a senior design team | Done |
| UC-12 | Assign students to senior design teams | Done |
| UC-13 | Remove a student from a senior design team | Done |
| UC-14 | Delete a senior design team | Done |
| UC-19 | Assign instructors to senior design teams | Done |
| UC-20 | Remove an instructor from a senior design team | Done |
| UC-28 | Submit peer evaluation for the previous week | Done |
| UC-29 | View own peer evaluation report | Not started |

---

### Shared — All Three

| UC | Description | Primary Owner | Status |
|----|-------------|---------------|--------|
| UC-27 | Student manages activities in a WAR | Emmanuel (lead) | Done |

---

## Changed Use Cases

| UC | Change | Reason |
|----|--------|--------|
| UC-11 | Automated email removed. System generates invite link; Admin sends manually. | No email service needed for 1-usage-per-year action. |
| UC-18 | Automated email removed. System generates instructor invite link; Admin sends manually. | Same reason as UC-11. Aligns all invite flows to the same pattern. |
| UC-30 | Reinstated. Instructor registers via invite link (not automated email). Was previously removed. | Restored to support revised UC-18 invite link flow. |

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

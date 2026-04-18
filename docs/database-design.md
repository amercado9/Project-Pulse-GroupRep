# Database Design

## Overview

MySQL database managed with Flyway migrations located in `src/main/resources/db/migration/`.

All migrations use the naming convention `V{n}__{description}.sql`.

---

## Tables

### users

Stores all user accounts (students, instructors, admins).

| Column     | Type         | Constraints                 | Notes                                             |
|------------|--------------|-----------------------------|---------------------------------------------------|
| id         | BIGINT       | PK, AUTO_INCREMENT          |                                                   |
| first_name | VARCHAR(100) | NOT NULL                    |                                                   |
| last_name  | VARCHAR(100) | NOT NULL                    |                                                   |
| email      | VARCHAR(255) | NOT NULL, UNIQUE            | Lowercase, used as login username                 |
| password   | VARCHAR(255) | NOT NULL                    | BCrypt-encoded                                    |
| roles      | VARCHAR(255) | NOT NULL, DEFAULT 'student' | Space-separated: `student`, `instructor`, `admin` |
| enabled    | BOOLEAN      | NOT NULL, DEFAULT TRUE      |                                                   |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP   |                                                   |

---

### sections

Senior design course sections.

| Column                         | Type         | Constraints               |
|--------------------------------|--------------|---------------------------|
| section_id                     | BIGINT       | PK, AUTO_INCREMENT        |
| section_name                   | VARCHAR(255) | NOT NULL                  |
| start_date                     | DATE         |                           |
| end_date                       | DATE         |                           |
| rubric_id                      | BIGINT       | FK → rubrics (future)     |
| course_id                      | BIGINT       | FK → courses (future)     |
| is_active                      | BOOLEAN      | NOT NULL, DEFAULT TRUE    |
| war_weekly_due_day             | VARCHAR(20)  | DEFAULT 'FRIDAY'          |
| war_due_time                   | VARCHAR(10)  | DEFAULT '23:59'           |
| peer_evaluation_weekly_due_day | VARCHAR(20)  | DEFAULT 'FRIDAY'          |
| peer_evaluation_due_time       | VARCHAR(10)  | DEFAULT '23:59'           |
| created_at                     | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP |

---

### section_active_weeks

| Column     | Type        | Constraints                                           |
|------------|-------------|-------------------------------------------------------|
| section_id | BIGINT      | NOT NULL, FK → sections(section_id) ON DELETE CASCADE |
| week       | VARCHAR(20) | NOT NULL — ISO week format e.g. `2026-W03`            |

---

### criteria

| Column       | Type         | Constraints               |
|--------------|--------------|---------------------------|
| criterion_id | BIGINT       | PK, AUTO_INCREMENT        |
| criterion    | VARCHAR(255) | NOT NULL                  |
| description  | TEXT         |                           |
| max_score    | INT          | NOT NULL, DEFAULT 10      |
| course_id    | BIGINT       | FK → courses (future)     |
| created_at   | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP |

---

### rubrics

| Column      | Type         | Constraints               |
|-------------|--------------|---------------------------|
| rubric_id   | BIGINT       | PK, AUTO_INCREMENT        |
| rubric_name | VARCHAR(255) | NOT NULL                  |
| course_id   | BIGINT       | FK → courses (future)     |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP |

---

### rubric_criteria

| Column       | Type   | Constraints                                       |
|--------------|--------|---------------------------------------------------|
| rubric_id    | BIGINT | PK, FK → rubrics(rubric_id) ON DELETE CASCADE     |
| criterion_id | BIGINT | PK, FK → criteria(criterion_id) ON DELETE CASCADE |

---

## Planned Tables (future migrations)

| Table        | Owner    | Related UCs         |
|--------------|----------|---------------------|
| teams        | Emmanuel | UC-7 to UC-14       |
| team_members | Emmanuel | UC-12, UC-13        |
| activities   | Emmanuel | UC-27, UC-32, UC-34 |
| evaluations  | Emmanuel | UC-28, UC-29, UC-33 |
| courses      | Alexa    | UC-2 to UC-6        |

---

## Seed Data

- `V2__schema.sql` — 3 sample sections (Spring 2026 A/B, Fall 2025 A)
- `V3__rubrics.sql` — 5 default criteria + 1 default rubric
- `DataInitializer.java` — seeds `admin@tcu.edu` on startup if not present

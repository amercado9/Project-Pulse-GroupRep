-- V2: Core schema — users and sections

-- Users (auth)
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name  VARCHAR(100)  NOT NULL,
    last_name   VARCHAR(100)  NOT NULL,
    email       VARCHAR(255)  NOT NULL UNIQUE,
    password    VARCHAR(255)  NOT NULL,
    roles       VARCHAR(255)  NOT NULL DEFAULT 'student',
    enabled     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

-- Sections
CREATE TABLE IF NOT EXISTS sections (
    section_id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_name                    VARCHAR(255) NOT NULL,
    start_date                      DATE,
    end_date                        DATE,
    rubric_id                       BIGINT,
    course_id                       BIGINT,
    is_active                       BOOLEAN      NOT NULL DEFAULT TRUE,
    war_weekly_due_day              VARCHAR(20)  DEFAULT 'FRIDAY',
    war_due_time                    VARCHAR(10)  DEFAULT '23:59',
    peer_evaluation_weekly_due_day  VARCHAR(20)  DEFAULT 'FRIDAY',
    peer_evaluation_due_time        VARCHAR(10)  DEFAULT '23:59',
    created_at                      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- Section active weeks
CREATE TABLE IF NOT EXISTS section_active_weeks (
    section_id  BIGINT       NOT NULL,
    week        VARCHAR(20)  NOT NULL,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

-- Seed sections for development
INSERT INTO sections (section_name, start_date, end_date, is_active, war_weekly_due_day, war_due_time, peer_evaluation_weekly_due_day, peer_evaluation_due_time)
VALUES
    ('Spring 2026 – Section A', '2026-01-13', '2026-05-10', TRUE,  'FRIDAY', '23:59', 'FRIDAY', '23:59'),
    ('Spring 2026 – Section B', '2026-01-13', '2026-05-10', TRUE,  'FRIDAY', '23:59', 'FRIDAY', '23:59'),
    ('Fall 2025 – Section A',   '2025-08-25', '2025-12-15', FALSE, 'FRIDAY', '23:59', 'FRIDAY', '23:59');

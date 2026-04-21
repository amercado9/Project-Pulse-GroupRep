-- V4: Teams, members, and instructors

CREATE TABLE IF NOT EXISTS teams (
    team_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id         BIGINT       NOT NULL,
    team_name          VARCHAR(255) NOT NULL,
    team_description   TEXT,
    team_website_url   VARCHAR(500),
    created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teams_team_name UNIQUE (team_name),
    CONSTRAINT fk_teams_section
        FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS team_students (
    team_id  BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_students_team
        FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    CONSTRAINT fk_team_students_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS team_instructors (
    team_id  BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    PRIMARY KEY (team_id, user_id),
    CONSTRAINT fk_team_instructors_team
        FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    CONSTRAINT fk_team_instructors_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Seed users for local/dev team discovery and login.
INSERT INTO users (id, first_name, last_name, email, password, roles, enabled)
VALUES
    (1001, 'Ivy',   'Stone',  'ivy.stone@tcu.edu',   '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'instructor', TRUE),
    (1002, 'Noah',  'Bennett','noah.bennett@tcu.edu','$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'instructor', TRUE),
    (1003, 'Ava',   'Johnson','ava.johnson@tcu.edu', '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1004, 'Liam',  'Parker', 'liam.parker@tcu.edu', '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1005, 'Mia',   'Chen',   'mia.chen@tcu.edu',    '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1006, 'Ethan', 'Wright', 'ethan.wright@tcu.edu','$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    password = VALUES(password),
    roles = VALUES(roles),
    enabled = VALUES(enabled);

INSERT INTO teams (team_id, section_id, team_name, team_description, team_website_url)
VALUES
    (2001, 1, 'Pulse Analytics', 'Analytics dashboard for tracking senior design team progress.', 'https://pulse-analytics.example.com'),
    (2002, 1, 'Peer Review Tool', 'Platform for collecting and summarizing peer evaluation feedback.', 'https://peer-review-tool.example.com'),
    (2003, 2, 'Requirements Hub', 'Centralized requirements workspace for senior design clients and teams.', 'https://requirements-hub.example.com')
ON DUPLICATE KEY UPDATE
    section_id = VALUES(section_id),
    team_description = VALUES(team_description),
    team_website_url = VALUES(team_website_url);

INSERT INTO team_students (team_id, user_id)
VALUES
    (2001, 1003),
    (2001, 1004),
    (2002, 1005),
    (2002, 1006),
    (2003, 1003),
    (2003, 1005)
ON DUPLICATE KEY UPDATE
    team_id = VALUES(team_id);

INSERT INTO team_instructors (team_id, user_id)
VALUES
    (2001, 1001),
    (2002, 1001),
    (2002, 1002),
    (2003, 1002)
ON DUPLICATE KEY UPDATE
    team_id = VALUES(team_id);

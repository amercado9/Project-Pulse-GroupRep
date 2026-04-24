-- V9: Seed activity data for UC-32/UC-34 WAR report testing
-- Team 2001 (Pulse Analytics): Ava Johnson (1003), Liam Parker (1004)
-- Team 2002 (Peer Review Tool): Mia Chen (1005), Ethan Wright (1006) — Ethan has no entries to demo "did not submit"

INSERT INTO users (id, first_name, last_name, email, password, roles, enabled)
VALUES
    (1001, 'Ivy',   'Stone',   'ivy.stone@tcu.edu',    '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'instructor', TRUE),
    (1002, 'Noah',  'Bennett', 'noah.bennett@tcu.edu', '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'instructor', TRUE),
    (1003, 'Ava',   'Johnson', 'ava.johnson@tcu.edu',  '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1004, 'Liam',  'Parker',  'liam.parker@tcu.edu',  '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1005, 'Mia',   'Chen',    'mia.chen@tcu.edu',     '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE),
    (1006, 'Ethan', 'Wright',  'ethan.wright@tcu.edu', '$2y$10$xsTQw5eM..haCE5neBk7AOEYqyvE65uyBlLZo.svoOKNKmrwfPLcG', 'student', TRUE)
ON DUPLICATE KEY UPDATE
    first_name = VALUES(first_name),
    last_name = VALUES(last_name),
    password = VALUES(password),
    roles = VALUES(roles),
    enabled = VALUES(enabled);

INSERT INTO teams (team_id, section_id, team_name, team_description, team_website_url)
SELECT
    2001,
    section_id,
    'Pulse Analytics',
    'Analytics dashboard for tracking senior design team progress.',
    'https://pulse-analytics.example.com'
FROM sections
WHERE section_name = 'Spring 2026 – Section A'
ON DUPLICATE KEY UPDATE
    section_id = VALUES(section_id),
    team_description = VALUES(team_description),
    team_website_url = VALUES(team_website_url);

INSERT INTO teams (team_id, section_id, team_name, team_description, team_website_url)
SELECT
    2002,
    section_id,
    'Peer Review Tool',
    'Platform for collecting and summarizing peer evaluation feedback.',
    'https://peer-review-tool.example.com'
FROM sections
WHERE section_name = 'Spring 2026 – Section A'
ON DUPLICATE KEY UPDATE
    section_id = VALUES(section_id),
    team_description = VALUES(team_description),
    team_website_url = VALUES(team_website_url);

INSERT INTO team_students (team_id, user_id)
VALUES
    (2001, 1003),
    (2001, 1004),
    (2002, 1005),
    (2002, 1006)
ON DUPLICATE KEY UPDATE
    team_id = VALUES(team_id);

INSERT IGNORE INTO activities (team_id, student_id, week, category, planned_activity, description, planned_hours, actual_hours, status) VALUES
    (2001, 1003, '2026-W16', 'BUGFIX',        'Fix login bug',       'Resolve the authentication redirect issue on mobile.', 4.00, 5.00, 'DONE'),
    (2001, 1003, '2026-W16', 'DOCUMENTATION', 'Write use cases',     'Write three new use cases for the registration flow.', 5.00, 3.00, 'IN_PROGRESS'),
    (2001, 1004, '2026-W16', 'DEVELOPMENT',   'Implement dashboard', 'Add activity summary chart to the team dashboard.',   10.00, 9.00, 'DONE'),
    (2002, 1005, '2026-W16', 'TESTING',       'Write unit tests',    'Cover AuthService with unit tests.',                   6.00, 6.50, 'DONE');

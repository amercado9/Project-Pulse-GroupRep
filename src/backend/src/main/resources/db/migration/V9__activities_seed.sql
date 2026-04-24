-- V9: Seed activity data for UC-32/UC-34 WAR report testing
-- Team 2001 (Pulse Analytics): Ava Johnson (1003), Liam Parker (1004)
-- Team 2002 (Peer Review Tool): Mia Chen (1005), Ethan Wright (1006) — Ethan has no entries to demo "did not submit"

INSERT IGNORE INTO activities (team_id, student_id, week, category, planned_activity, description, planned_hours, actual_hours, status) VALUES
    (2001, 1003, '2026-W16', 'BUGFIX',        'Fix login bug',       'Resolve the authentication redirect issue on mobile.', 4.00, 5.00, 'DONE'),
    (2001, 1003, '2026-W16', 'DOCUMENTATION', 'Write use cases',     'Write three new use cases for the registration flow.', 5.00, 3.00, 'IN_PROGRESS'),
    (2001, 1004, '2026-W16', 'DEVELOPMENT',   'Implement dashboard', 'Add activity summary chart to the team dashboard.',   10.00, 9.00, 'DONE'),
    (2002, 1005, '2026-W16', 'TESTING',       'Write unit tests',    'Cover AuthService with unit tests.',                   6.00, 6.50, 'DONE');

-- V6: User section membership for student assignment

ALTER TABLE users
    ADD COLUMN section_id BIGINT NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_section
        FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE SET NULL;

-- Resolve each student's section membership deterministically to the minimum
-- section_id across their existing team memberships.
UPDATE users u
JOIN (
    SELECT ts.user_id, MIN(t.section_id) AS resolved_section_id
    FROM team_students ts
    JOIN teams t ON t.team_id = ts.team_id
    GROUP BY ts.user_id
) resolved ON resolved.user_id = u.id
SET u.section_id = resolved.resolved_section_id
WHERE LOWER(CONCAT(' ', u.roles, ' ')) LIKE '% student %';

-- Remove cross-section memberships that do not match the resolved student section.
DELETE ts
FROM team_students ts
JOIN teams t ON t.team_id = ts.team_id
JOIN users u ON u.id = ts.user_id
WHERE u.section_id IS NOT NULL
  AND t.section_id <> u.section_id;

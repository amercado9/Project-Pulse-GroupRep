-- Enforce one team per student
ALTER TABLE team_students ADD CONSTRAINT uq_team_students_user UNIQUE (user_id);

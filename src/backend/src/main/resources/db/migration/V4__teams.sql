-- V4: Teams and Instructor assignments
CREATE TABLE IF NOT EXISTS teams (
    team_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name         VARCHAR(255) NOT NULL,
    description       TEXT,
    team_website_url  VARCHAR(255),
    section_id        BIGINT NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (section_id) REFERENCES sections(section_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS team_instructors (
    team_id        BIGINT NOT NULL,
    instructor_id  BIGINT NOT NULL,
    PRIMARY KEY (team_id, instructor_id),
    FOREIGN KEY (team_id)       REFERENCES teams(team_id) ON DELETE CASCADE,
    FOREIGN KEY (instructor_id) REFERENCES users(id)      ON DELETE CASCADE
);

-- Seed some teams for development
INSERT INTO teams (team_name, description, team_website_url, section_id)
VALUES
    ('Team Alpha', 'Alpha team description', 'http://alpha.example.com', 1),
    ('Team Beta',  'Beta team description',  'http://beta.example.com',  1),
    ('Team Gamma', 'Gamma team description', 'http://gamma.example.com', 2);

-- Assign instructor (assuming user id 1 is an instructor) to some teams
-- We will use the DataInitializer to ensure we have an instructor

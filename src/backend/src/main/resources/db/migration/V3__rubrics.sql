-- V3: Rubrics and Criteria schema

CREATE TABLE IF NOT EXISTS criteria (
    criterion_id  BIGINT        AUTO_INCREMENT PRIMARY KEY,
    criterion     VARCHAR(255)  NOT NULL,
    description   TEXT,
    max_score     INT           NOT NULL DEFAULT 10,
    course_id     BIGINT,
    created_at    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rubrics (
    rubric_id    BIGINT        AUTO_INCREMENT PRIMARY KEY,
    rubric_name  VARCHAR(255)  NOT NULL,
    course_id    BIGINT,
    created_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rubric_criteria (
    rubric_id     BIGINT NOT NULL,
    criterion_id  BIGINT NOT NULL,
    PRIMARY KEY (rubric_id, criterion_id),
    FOREIGN KEY (rubric_id)    REFERENCES rubrics(rubric_id)   ON DELETE CASCADE,
    FOREIGN KEY (criterion_id) REFERENCES criteria(criterion_id) ON DELETE CASCADE
);

-- Seed criteria
INSERT INTO criteria (criterion, description, max_score) VALUES
    ('Participation',    'Active engagement in team meetings and project activities.',       10),
    ('Code Quality',     'Adherence to coding standards, readability, and documentation.',  10),
    ('Communication',    'Clarity and frequency of communication with teammates.',          10),
    ('Deliverables',     'Timely completion of assigned tasks and milestones.',              10),
    ('Problem Solving',  'Ability to identify and resolve technical or team challenges.',   10);

-- Seed a default rubric with all criteria assigned
INSERT INTO rubrics (rubric_name) VALUES ('Default Peer Evaluation Rubric');

INSERT INTO rubric_criteria (rubric_id, criterion_id)
SELECT r.rubric_id, c.criterion_id
FROM rubrics r, criteria c
WHERE r.rubric_name = 'Default Peer Evaluation Rubric';

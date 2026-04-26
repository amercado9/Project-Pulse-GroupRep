-- V7: Weekly activity report activities

CREATE TABLE IF NOT EXISTS activities (
    activity_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id           BIGINT         NOT NULL,
    student_id        BIGINT         NOT NULL,
    week              VARCHAR(20)    NOT NULL,
    category          VARCHAR(50)    NOT NULL,
    planned_activity  VARCHAR(255)   NOT NULL,
    description       TEXT           NOT NULL,
    planned_hours     DECIMAL(6,2)   NOT NULL,
    actual_hours      DECIMAL(6,2)   NOT NULL,
    status            VARCHAR(30)    NOT NULL,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_activities_team
        FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    CONSTRAINT fk_activities_student
        FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_activities_student_week ON activities (student_id, week);
CREATE INDEX idx_activities_team_week ON activities (team_id, week);
CREATE INDEX idx_activities_team_week_student ON activities (team_id, week, student_id);

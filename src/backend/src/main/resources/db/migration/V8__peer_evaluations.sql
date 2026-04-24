-- V8: Peer evaluations

CREATE TABLE IF NOT EXISTS evaluation_submissions (
    submission_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id               BIGINT      NOT NULL,
    evaluator_student_id  BIGINT      NOT NULL,
    week                  VARCHAR(20) NOT NULL,
    created_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_evaluation_submissions_team
        FOREIGN KEY (team_id) REFERENCES teams(team_id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_submissions_evaluator
        FOREIGN KEY (evaluator_student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_evaluation_submissions_evaluator_week
        UNIQUE (evaluator_student_id, week)
);

CREATE TABLE IF NOT EXISTS evaluation_entries (
    entry_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id          BIGINT NOT NULL,
    evaluatee_student_id   BIGINT NOT NULL,
    public_comment         TEXT,
    private_comment        TEXT,
    CONSTRAINT fk_evaluation_entries_submission
        FOREIGN KEY (submission_id) REFERENCES evaluation_submissions(submission_id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_entries_evaluatee
        FOREIGN KEY (evaluatee_student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_evaluation_entries_submission_evaluatee
        UNIQUE (submission_id, evaluatee_student_id)
);

CREATE TABLE IF NOT EXISTS evaluation_scores (
    entry_id       BIGINT NOT NULL,
    criterion_id   BIGINT NOT NULL,
    score          INT    NOT NULL,
    PRIMARY KEY (entry_id, criterion_id),
    CONSTRAINT fk_evaluation_scores_entry
        FOREIGN KEY (entry_id) REFERENCES evaluation_entries(entry_id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_scores_criterion
        FOREIGN KEY (criterion_id) REFERENCES criteria(criterion_id) ON DELETE CASCADE
);

CREATE INDEX idx_evaluation_submissions_week
    ON evaluation_submissions (week);
CREATE INDEX idx_evaluation_submissions_team_week
    ON evaluation_submissions (team_id, week);
CREATE INDEX idx_evaluation_submissions_evaluator_week
    ON evaluation_submissions (evaluator_student_id, week);
CREATE INDEX idx_evaluation_entries_evaluatee
    ON evaluation_entries (evaluatee_student_id);

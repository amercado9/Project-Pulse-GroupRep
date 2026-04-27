package team.projectpulse.rubric;

import jakarta.persistence.*;

@Entity
@Table(name = "criteria")
public class Criterion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "criterion_id")
    private Long criterionId;

    @Column(nullable = false)
    private String criterion;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "max_score", nullable = false)
    private int maxScore = 10;

    @Column(name = "course_id")
    private Long courseId;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getCriterionId()          { return criterionId; }
    public void setCriterionId(Long v)    { this.criterionId = v; }

    public String getCriterion()          { return criterion; }
    public void setCriterion(String v)    { this.criterion = v; }

    public String getDescription()        { return description; }
    public void setDescription(String v)  { this.description = v; }

    public int getMaxScore()              { return maxScore; }
    public void setMaxScore(int v)        { this.maxScore = v; }

    public Long getCourseId()             { return courseId; }
    public void setCourseId(Long v)       { this.courseId = v; }
}

package team.projectpulse.rubric.domain;

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
    private double maxScore;

    public Long getCriterionId()          { return criterionId; }
    public void setCriterionId(Long v)    { this.criterionId = v; }

    public String getCriterion()          { return criterion; }
    public void setCriterion(String v)    { this.criterion = v; }

    public String getDescription()        { return description; }
    public void setDescription(String v)  { this.description = v; }

    public double getMaxScore()           { return maxScore; }
    public void setMaxScore(double v)     { this.maxScore = v; }
}

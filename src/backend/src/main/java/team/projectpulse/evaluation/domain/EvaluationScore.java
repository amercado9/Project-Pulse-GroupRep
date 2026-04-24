package team.projectpulse.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import team.projectpulse.rubric.domain.Criterion;

@Entity
@Table(name = "evaluation_scores")
public class EvaluationScore {

    @EmbeddedId
    private EvaluationScoreId id = new EvaluationScoreId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("entryId")
    @JoinColumn(name = "entry_id", nullable = false)
    private EvaluationEntry entry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("criterionId")
    @JoinColumn(name = "criterion_id", nullable = false)
    private Criterion criterion;

    @Column(name = "score", nullable = false)
    private Integer score;

    public EvaluationScoreId getId() {
        return id;
    }

    public void setId(EvaluationScoreId id) {
        this.id = id;
    }

    public EvaluationEntry getEntry() {
        return entry;
    }

    public void setEntry(EvaluationEntry entry) {
        this.entry = entry;
    }

    public Criterion getCriterion() {
        return criterion;
    }

    public void setCriterion(Criterion criterion) {
        this.criterion = criterion;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}

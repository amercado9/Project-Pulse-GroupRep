package team.projectpulse.evaluation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EvaluationScoreId implements Serializable {

    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "criterion_id")
    private Long criterionId;

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Long getCriterionId() {
        return criterionId;
    }

    public void setCriterionId(Long criterionId) {
        this.criterionId = criterionId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof EvaluationScoreId other)) {
            return false;
        }
        return Objects.equals(entryId, other.entryId)
            && Objects.equals(criterionId, other.criterionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entryId, criterionId);
    }
}

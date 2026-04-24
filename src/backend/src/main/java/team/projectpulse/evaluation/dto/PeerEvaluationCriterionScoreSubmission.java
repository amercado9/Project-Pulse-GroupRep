package team.projectpulse.evaluation.dto;

import java.math.BigDecimal;

public record PeerEvaluationCriterionScoreSubmission(
    Long criterionId,
    BigDecimal score
) {
}

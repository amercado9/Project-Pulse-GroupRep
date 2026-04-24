package team.projectpulse.report.dto;

import java.math.BigDecimal;

public record StudentPeerEvaluationCriterionAverageDto(
    Long criterionId,
    String criterion,
    String description,
    BigDecimal averageScore,
    double maxScore
) {
}

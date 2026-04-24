package team.projectpulse.evaluation.dto;

public record EvaluationCriterionDto(
    Long criterionId,
    String criterion,
    String description,
    double maxScore
) {
}

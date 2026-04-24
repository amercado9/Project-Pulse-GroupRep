package team.projectpulse.evaluation.dto;

import java.util.List;

public record EvaluationMemberDraftDto(
    Long studentId,
    String fullName,
    boolean self,
    String publicComment,
    String privateComment,
    List<EvaluationCriterionScoreDraftDto> scores
) {
}

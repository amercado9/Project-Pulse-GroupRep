package team.projectpulse.evaluation.dto;

import java.util.List;

public record PeerEvaluationMemberSubmission(
    Long evaluateeStudentId,
    String publicComment,
    String privateComment,
    List<PeerEvaluationCriterionScoreSubmission> criterionScores
) {
}

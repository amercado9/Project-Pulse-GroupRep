package team.projectpulse.evaluation.dto;

import java.util.List;

public record PeerEvaluationSubmissionRequest(
    List<PeerEvaluationMemberSubmission> evaluations
) {
}

package team.projectpulse.evaluation.dto;

import java.time.LocalDateTime;

public record PeerEvaluationSubmissionDetail(
    Long submissionId,
    String week,
    LocalDateTime submittedAt,
    LocalDateTime updatedAt
) {
}

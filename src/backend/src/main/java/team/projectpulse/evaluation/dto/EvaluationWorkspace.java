package team.projectpulse.evaluation.dto;

import java.util.List;

public record EvaluationWorkspace(
    Long sectionId,
    String sectionName,
    Long teamId,
    String teamName,
    String week,
    String weekLabel,
    String weekRangeLabel,
    Long submissionId,
    boolean submitted,
    boolean editable,
    boolean canSubmit,
    String availabilityMessage,
    String dueAt,
    List<EvaluationCriterionDto> criteria,
    List<EvaluationMemberDraftDto> members
) {
}

package team.projectpulse.team.dto;

import java.util.List;

public record TeamStudentAssignmentInput(
    Long teamId,
    List<Long> studentIds
) {
}

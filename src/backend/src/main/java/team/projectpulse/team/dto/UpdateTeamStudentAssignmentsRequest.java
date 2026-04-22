package team.projectpulse.team.dto;

import java.util.List;

public record UpdateTeamStudentAssignmentsRequest(
    Long sectionId,
    List<TeamStudentAssignmentInput> assignments
) {
}

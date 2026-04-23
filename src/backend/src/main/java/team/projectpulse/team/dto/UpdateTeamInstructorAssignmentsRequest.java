package team.projectpulse.team.dto;

import java.util.List;

public record UpdateTeamInstructorAssignmentsRequest(
    Long sectionId,
    List<TeamInstructorAssignmentInput> assignments
) {
}

package team.projectpulse.team.dto;

import java.util.List;

public record TeamInstructorAssignmentTeam(
    Long teamId,
    String teamName,
    List<TeamInstructorAssignmentInstructor> assignedInstructors
) {
}

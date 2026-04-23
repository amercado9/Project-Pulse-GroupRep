package team.projectpulse.team.dto;

import java.util.List;

public record InstructorAssignmentCandidate(
    Long instructorId,
    String fullName,
    String email,
    List<String> assignedTeamNames
) {
}

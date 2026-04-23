package team.projectpulse.team.dto;

import java.util.List;

public record TeamInstructorAssignmentWorkspace(
    Long sectionId,
    String sectionName,
    List<TeamInstructorAssignmentTeam> teams,
    List<InstructorAssignmentCandidate> instructors
) {
}

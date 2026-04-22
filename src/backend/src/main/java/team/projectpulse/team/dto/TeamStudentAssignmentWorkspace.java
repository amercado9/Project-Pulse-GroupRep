package team.projectpulse.team.dto;

import java.util.List;

public record TeamStudentAssignmentWorkspace(
    Long sectionId,
    String sectionName,
    List<TeamStudentAssignmentTeam> teams,
    List<StudentAssignmentCandidate> students
) {
}

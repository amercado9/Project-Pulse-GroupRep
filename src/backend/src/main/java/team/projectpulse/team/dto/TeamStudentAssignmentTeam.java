package team.projectpulse.team.dto;

import java.util.List;

public record TeamStudentAssignmentTeam(
    Long teamId,
    String teamName,
    List<StudentAssignmentCandidate> assignedStudents
) {
}

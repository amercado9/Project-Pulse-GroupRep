package team.projectpulse.team.dto;

import java.util.List;

public record TeamInstructorAssignmentInput(
    Long teamId,
    List<Long> instructorIds
) {
}

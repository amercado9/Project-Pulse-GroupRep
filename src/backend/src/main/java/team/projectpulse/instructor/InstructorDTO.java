package team.projectpulse.instructor;

import java.util.List;
import java.util.Map;

public record InstructorDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    boolean enabled,
    String roles,
    Map<String, List<TeamBriefDTO>> supervisedTeamsBySection
) {}

record TeamBriefDTO(
    Long teamId,
    String teamName
) {}

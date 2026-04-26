package team.projectpulse.user.dto;

import java.util.List;

public record StudentDetail(
    Long studentId,
    String firstName,
    String lastName,
    String email,
    boolean enabled,
    Long sectionId,
    String sectionName,
    List<String> teamNames
) {}

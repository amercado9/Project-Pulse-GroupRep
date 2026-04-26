package team.projectpulse.user.dto;

public record StudentSummary(
    Long studentId,
    String firstName,
    String lastName,
    String email,
    boolean enabled,
    Long sectionId,
    String sectionName
) {}

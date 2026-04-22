package team.projectpulse.team.dto;

public record CreateTeamRequest(
    Long sectionId,
    String teamName,
    String teamDescription,
    String teamWebsiteUrl
) {
}

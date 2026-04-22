package team.projectpulse.team.dto;

public record UpdateTeamRequest(
    String teamName,
    String teamDescription,
    String teamWebsiteUrl
) {
}

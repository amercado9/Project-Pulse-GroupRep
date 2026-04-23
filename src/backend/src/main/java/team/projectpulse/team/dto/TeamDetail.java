package team.projectpulse.team.dto;

import java.util.List;

public record TeamDetail(
    Long teamId,
    Long sectionId,
    String sectionName,
    String teamName,
    String teamDescription,
    String teamWebsiteUrl,
    List<TeamMemberDetail> teamMembers,
    List<String> teamMemberNames,
    List<String> instructorNames
) {
}

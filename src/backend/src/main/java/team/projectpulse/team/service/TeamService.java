package team.projectpulse.team.service;

import org.springframework.stereotype.Service;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;

import java.util.List;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public List<TeamSummary> findTeams(Long sectionId, String sectionName, String teamName, String instructor) {
        List<Team> teams = teamRepository.search(
            sectionId,
            normalize(sectionName),
            normalize(teamName),
            normalize(instructor)
        );

        return teams.stream()
            .map(this::toSummary)
            .toList();
    }

    private TeamSummary toSummary(Team team) {
        List<String> teamMemberNames = team.getStudents().stream()
            .map(this::toDisplayName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        List<String> instructorNames = team.getInstructors().stream()
            .map(this::toDisplayName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();

        return new TeamSummary(
            team.getTeamId(),
            team.getSection().getSectionId(),
            team.getSection().getSectionName(),
            team.getTeamName(),
            team.getTeamDescription(),
            team.getTeamWebsiteUrl(),
            teamMemberNames,
            instructorNames
        );
    }

    private String toDisplayName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

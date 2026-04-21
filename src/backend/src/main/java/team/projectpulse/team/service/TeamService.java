package team.projectpulse.team.service;

import org.springframework.stereotype.Service;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamNotFoundException;
import team.projectpulse.team.dto.TeamDetail;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;

import java.util.List;
import java.util.stream.StreamSupport;

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

    public TeamDetail findTeamDetail(Long id) {
        Team team = teamRepository.findDetailById(id)
            .orElseThrow(() -> new TeamNotFoundException(id));

        return toDetail(team);
    }

    private TeamSummary toSummary(Team team) {
        return new TeamSummary(
            team.getTeamId(),
            team.getSection().getSectionId(),
            team.getSection().getSectionName(),
            team.getTeamName(),
            team.getTeamDescription(),
            team.getTeamWebsiteUrl(),
            toSortedNames(team.getStudents()),
            toSortedNames(team.getInstructors())
        );
    }

    private TeamDetail toDetail(Team team) {
        return new TeamDetail(
            team.getTeamId(),
            team.getSection().getSectionId(),
            team.getSection().getSectionName(),
            team.getTeamName(),
            team.getTeamDescription(),
            team.getTeamWebsiteUrl(),
            toSortedNames(team.getStudents()),
            toSortedNames(team.getInstructors())
        );
    }

    private String toDisplayName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private List<String> toSortedNames(Iterable<User> users) {
        return StreamSupport.stream(users.spliterator(), false)
            .map(this::toDisplayName)
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .toList();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

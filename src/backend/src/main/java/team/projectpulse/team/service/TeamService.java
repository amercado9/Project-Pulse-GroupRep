package team.projectpulse.team.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.StudentNotInTeamException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamAlreadyExistsException;
import team.projectpulse.team.domain.TeamNotFoundException;
import team.projectpulse.team.dto.CreateTeamRequest;
import team.projectpulse.team.dto.TeamDetail;
import team.projectpulse.team.dto.TeamMember;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.dto.UpdateTeamRequest;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final SectionRepository sectionRepository;

    public TeamService(TeamRepository teamRepository, SectionRepository sectionRepository) {
        this.teamRepository = teamRepository;
        this.sectionRepository = sectionRepository;
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

    public TeamDetail createTeam(CreateTeamRequest request) {
        if (request == null) {
            throw new InvalidTeamException("Team request is required.");
        }

        if (request.sectionId() == null) {
            throw new InvalidTeamException("Section is required.");
        }

        String normalizedName = requireTeamName(request.teamName());
        String normalizedDescription = normalizeNullable(request.teamDescription());
        String normalizedWebsite = normalizeNullable(request.teamWebsiteUrl());

        validateWebsite(normalizedWebsite);

        Section section = sectionRepository.findById(request.sectionId())
            .orElseThrow(() -> new team.projectpulse.section.domain.SectionNotFoundException(request.sectionId()));

        if (teamRepository.existsByTeamNameIgnoreCase(normalizedName)) {
            throw new TeamAlreadyExistsException(normalizedName);
        }

        Team team = new Team();
        team.setSection(section);
        team.setTeamName(normalizedName);
        team.setTeamDescription(normalizedDescription);
        team.setTeamWebsiteUrl(normalizedWebsite);

        return toDetail(teamRepository.save(team));
    }

    public TeamDetail updateTeam(Long id, UpdateTeamRequest request) {
        if (request == null) {
            throw new InvalidTeamException("Team request is required.");
        }

        Team team = teamRepository.findDetailById(id)
            .orElseThrow(() -> new TeamNotFoundException(id));

        String normalizedName = requireTeamName(request.teamName());
        String normalizedDescription = normalizeNullable(request.teamDescription());
        String normalizedWebsite = normalizeNullable(request.teamWebsiteUrl());

        validateWebsite(normalizedWebsite);

        if (teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot(normalizedName, id)) {
            throw new TeamAlreadyExistsException(normalizedName);
        }

        team.setTeamName(normalizedName);
        team.setTeamDescription(normalizedDescription);
        team.setTeamWebsiteUrl(normalizedWebsite);

        return toDetail(teamRepository.save(team));
    }

    public TeamDetail removeStudentFromTeam(Long teamId, Long studentId) {
        Team team = teamRepository.findDetailById(teamId)
            .orElseThrow(() -> new TeamNotFoundException(teamId));

        User student = team.getStudents().stream()
            .filter(u -> u.getId().equals(studentId))
            .findFirst()
            .orElseThrow(() -> new StudentNotInTeamException(studentId, teamId));

        team.getStudents().remove(student);
        TeamDetail updated = toDetail(teamRepository.save(team));

        log.info("Student {} ({}) removed from team {} ({}) by admin.",
                studentId, student.getEmail(), teamId, team.getTeamName());

        return updated;
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
            toMembers(team.getStudents()),
            toSortedNames(team.getInstructors())
        );
    }

    private List<TeamMember> toMembers(Iterable<User> users) {
        return StreamSupport.stream(users.spliterator(), false)
            .map(u -> new TeamMember(u.getId(), (u.getFirstName() + " " + u.getLastName()).trim()))
            .sorted(Comparator.comparing(TeamMember::fullName, String.CASE_INSENSITIVE_ORDER))
            .toList();
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

    private String normalizeNullable(String value) {
        return normalize(value);
    }

    private String requireTeamName(String teamName) {
        String normalizedName = normalize(teamName);
        if (normalizedName == null) {
            throw new InvalidTeamException("Team name is required.");
        }
        if (normalizedName.length() > 255) {
            throw new InvalidTeamException("Team name must be 255 characters or fewer.");
        }
        return normalizedName;
    }

    private void validateWebsite(String websiteUrl) {
        if (websiteUrl == null) {
            return;
        }
        if (websiteUrl.length() > 500) {
            throw new InvalidTeamException("Team website URL must be 500 characters or fewer.");
        }
        try {
            URI uri = new URI(websiteUrl);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new InvalidTeamException("Team website URL must start with http:// or https://.");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidTeamException("Team website URL must be a valid absolute URL.");
            }
        } catch (URISyntaxException ex) {
            throw new InvalidTeamException("Team website URL must be a valid absolute URL.");
        }
    }
}

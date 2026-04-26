package team.projectpulse.user.service;

import org.springframework.stereotype.Service;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.dto.InstructorSummary;
import team.projectpulse.user.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public InstructorService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public List<InstructorSummary> searchInstructors(
            String firstName, String lastName, Boolean enabled, String teamName) {

        List<User> instructors = userRepository.searchInstructors(
                normalize(firstName),
                normalize(lastName),
                enabled,
                normalize(teamName)
        );

        return instructors.stream()
                .map(this::toSummary)
                .sorted(Comparator
                        .comparingInt((InstructorSummary s) ->
                                s.academicYear() != null ? s.academicYear() : Integer.MIN_VALUE)
                        .reversed()
                        .thenComparing(s -> s.lastName().toLowerCase()))
                .toList();
    }

    private InstructorSummary toSummary(User instructor) {
        List<Team> teams = teamRepository.findAllByInstructorId(instructor.getId());
        List<String> teamNames = teams.stream().map(Team::getTeamName).toList();
        Integer academicYear = teams.isEmpty()
                ? null
                : teams.get(0).getSection().getStartDate().getYear();

        return new InstructorSummary(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                instructor.isEnabled(),
                teamNames,
                academicYear
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

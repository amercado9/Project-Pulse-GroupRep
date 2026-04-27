package team.projectpulse.instructor;

import org.springframework.stereotype.Service;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;
import team.projectpulse.user.User;
import team.projectpulse.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InstructorService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public InstructorService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public InstructorDTO getInstructorDetails(Long instructorId) {
        User user = userRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found with id: " + instructorId));

        if (!user.getRoles().contains("instructor") && !user.getRoles().contains("admin")) {
            throw new RuntimeException("User is not an instructor");
        }

        List<Team> supervisedTeams = teamRepository.findByInstructorsId(instructorId);

        Map<String, List<TeamBriefDTO>> groupedTeams = supervisedTeams.stream()
                .collect(Collectors.groupingBy(
                        team -> team.getSection().getSectionName(),
                        Collectors.mapping(
                                team -> new TeamBriefDTO(team.getTeamId(), team.getTeamName()),
                                Collectors.toList()
                        )
                ));

        return new InstructorDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRoles(),
                groupedTeams
        );
    }
}

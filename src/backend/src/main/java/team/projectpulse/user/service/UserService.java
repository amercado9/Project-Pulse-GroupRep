package team.projectpulse.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.domain.UserNotFoundException;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.dto.StudentTeamInfo;
import team.projectpulse.user.repository.UserRepository;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public UserService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for: " + email));
    }

    public List<StudentSummary> findStudents(String firstName, String lastName, String email,
                                             Long teamId, String teamName,
                                             Long sectionId, String sectionName) {
        List<User> students = userRepository.searchStudents(
                normalize(firstName), normalize(lastName), normalize(email),
                teamId, normalize(teamName), sectionId, normalize(sectionName)
        );

        return students.stream()
                .map(u -> {
                    List<String> teamNames = teamRepository.findByStudentId(u.getId())
                            .stream()
                            .map(Team::getTeamName)
                            .toList();
                    return new StudentSummary(u.getId(), u.getFirstName(), u.getLastName(), teamNames);
                })
                .toList();
    }

    public StudentDetail getStudentDetail(Long id) {
        User user = userRepository.findById(id)
                .filter(u -> u.getRoles().contains("student"))
                .orElseThrow(() -> new UserNotFoundException(id));

        List<StudentTeamInfo> teams = teamRepository.findByStudentId(id)
                .stream()
                .map(t -> new StudentTeamInfo(
                        t.getTeamId(),
                        t.getTeamName(),
                        t.getSection().getSectionId(),
                        t.getSection().getSectionName()
                ))
                .toList();

        return new StudentDetail(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                teams,
                List.of(),
                List.of()
        );
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

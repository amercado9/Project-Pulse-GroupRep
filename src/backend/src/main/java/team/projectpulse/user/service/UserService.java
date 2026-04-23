package team.projectpulse.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.dto.StudentSummary;
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

    public List<StudentSummary> findStudents(
        String firstName,
        String lastName,
        String email,
        String sectionName,
        String teamName
    ) {
        List<User> students = userRepository.searchStudents(
            blankToNull(firstName),
            blankToNull(lastName),
            blankToNull(email),
            blankToNull(sectionName),
            blankToNull(teamName)
        );

        return students.stream().map(u -> {
            List<String> teamNames = teamRepository.findByStudentId(u.getId())
                    .stream()
                    .map(t -> t.getTeamName())
                    .toList();
            String section = u.getSection() != null ? u.getSection().getSectionName() : null;
            return new StudentSummary(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), section, teamNames);
        }).toList();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}

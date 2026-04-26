package team.projectpulse.user.service;

import org.springframework.stereotype.Service;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.StudentNotFoundException;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.repository.UserRepository;

import java.util.List;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public StudentService(UserRepository userRepository, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    public List<StudentSummary> findAllStudents() {
        return userRepository.findAllStudents().stream()
                .map(this::toStudentSummary)
                .toList();
    }

    public StudentDetail findStudentById(Long id) {
        User user = userRepository.findStudentById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        List<String> teamNames = teamRepository.findAllByStudentId(id).stream()
                .map(t -> t.getTeamName())
                .toList();
        return toStudentDetail(user, teamNames);
    }

    public void deleteStudent(Long id) {
        User user = userRepository.findStudentById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
        userRepository.delete(user);
    }

    private StudentSummary toStudentSummary(User user) {
        return new StudentSummary(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.getSection() != null ? user.getSection().getSectionId() : null,
                user.getSection() != null ? user.getSection().getSectionName() : null
        );
    }

    private StudentDetail toStudentDetail(User user, List<String> teamNames) {
        return new StudentDetail(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.getSection() != null ? user.getSection().getSectionId() : null,
                user.getSection() != null ? user.getSection().getSectionName() : null,
                teamNames
        );
    }
}

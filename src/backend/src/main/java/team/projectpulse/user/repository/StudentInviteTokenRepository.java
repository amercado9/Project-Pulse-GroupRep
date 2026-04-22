package team.projectpulse.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.user.domain.StudentInviteToken;

import java.util.Optional;

public interface StudentInviteTokenRepository extends JpaRepository<StudentInviteToken, Long> {
    Optional<StudentInviteToken> findByToken(String token);
    Optional<StudentInviteToken> findByEmailIgnoreCaseAndSectionId(String email, Long sectionId);
}

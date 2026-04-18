package team.projectpulse.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
}

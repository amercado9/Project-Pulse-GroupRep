package team.projectpulse.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        select u from User u
        left join fetch u.section s
        where u.enabled = true
          and s.sectionId = :sectionId
          and lower(concat(' ', coalesce(u.roles, ''), ' ')) like '% student %'
        order by lower(u.lastName), lower(u.firstName), lower(u.email)
        """)
    List<User> findEnabledStudentsBySectionId(@Param("sectionId") Long sectionId);
}

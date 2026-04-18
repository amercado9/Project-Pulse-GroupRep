package team.projectpulse.rubric.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.rubric.domain.Rubric;

public interface RubricRepository extends JpaRepository<Rubric, Long> {
    boolean existsByRubricNameIgnoreCase(String rubricName);
}

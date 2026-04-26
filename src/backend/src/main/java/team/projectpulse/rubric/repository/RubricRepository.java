package team.projectpulse.rubric.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.rubric.domain.Rubric;

import java.util.Optional;

public interface RubricRepository extends JpaRepository<Rubric, Long> {
    boolean existsByRubricNameIgnoreCase(String rubricName);

    @Query("""
        select distinct r from Rubric r
        left join fetch r.criteria c
        where r.rubricId = :rubricId
        order by c.criterionId asc
        """)
    Optional<Rubric> findByIdWithCriteria(@Param("rubricId") Long rubricId);
}

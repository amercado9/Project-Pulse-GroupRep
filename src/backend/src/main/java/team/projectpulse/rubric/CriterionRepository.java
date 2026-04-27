package team.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CriterionRepository extends JpaRepository<Criterion, Long> {

    @Query("""
            SELECT c FROM Criterion c
            WHERE (:criterionId IS NULL OR c.criterionId = :criterionId)
              AND (:criterion   IS NULL OR LOWER(c.criterion) LIKE LOWER(CONCAT('%', :criterion, '%')))
            ORDER BY c.criterion ASC
            """)
    List<Criterion> searchByCriteria(
            @Param("criterionId") Long criterionId,
            @Param("criterion") String criterion
    );
}

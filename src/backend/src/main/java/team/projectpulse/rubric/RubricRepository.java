package team.projectpulse.rubric;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RubricRepository extends JpaRepository<Rubric, Long> {

    @Query("""
            SELECT r FROM Rubric r
            WHERE (:rubricId   IS NULL OR r.rubricId = :rubricId)
              AND (:rubricName IS NULL OR LOWER(r.rubricName) LIKE LOWER(CONCAT('%', :rubricName, '%')))
            ORDER BY r.rubricName ASC
            """)
    List<Rubric> searchByCriteria(
            @Param("rubricId") Long rubricId,
            @Param("rubricName") String rubricName
    );
}

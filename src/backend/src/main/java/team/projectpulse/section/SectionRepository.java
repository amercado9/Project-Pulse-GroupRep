package team.projectpulse.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SectionRepository extends JpaRepository<Section, Long> {

    boolean existsBySectionNameIgnoreCase(String sectionName);

    @Query("""
            SELECT s FROM Section s
            WHERE (:sectionId IS NULL OR s.sectionId = :sectionId)
              AND (:sectionName IS NULL OR LOWER(s.sectionName) LIKE LOWER(CONCAT('%', :sectionName, '%')))
            """)
    Page<Section> searchByCriteria(
            @Param("sectionId") Long sectionId,
            @Param("sectionName") String sectionName,
            Pageable pageable
    );
}

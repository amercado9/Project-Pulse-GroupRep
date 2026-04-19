package team.projectpulse.section.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import team.projectpulse.section.domain.Section;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findBySectionNameContainingIgnoreCaseOrderBySectionNameDesc(String sectionName);
    List<Section> findAllByOrderBySectionNameDesc();
}

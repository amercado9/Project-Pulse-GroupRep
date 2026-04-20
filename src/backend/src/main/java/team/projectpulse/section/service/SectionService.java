package team.projectpulse.section.service;

import org.springframework.stereotype.Service;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.dto.SectionDetail;
import team.projectpulse.section.dto.SectionSummary;
import team.projectpulse.section.repository.SectionRepository;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final RubricRepository rubricRepository;

    public SectionService(SectionRepository sectionRepository, RubricRepository rubricRepository) {
        this.sectionRepository = sectionRepository;
        this.rubricRepository = rubricRepository;
    }

    public List<SectionSummary> findSections(String name) {
        List<Section> sections = (name != null && !name.isBlank())
                ? sectionRepository.findBySectionNameContainingIgnoreCaseOrderBySectionNameDesc(name.trim())
                : sectionRepository.findAllByOrderBySectionNameDesc();

        return sections.stream()
                .map(s -> new SectionSummary(s.getSectionId(), s.getSectionName(), List.of()))
                .toList();
    }

    public SectionDetail findSectionDetail(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));

        String rubricName = null;
        if (section.getRubricId() != null) {
            rubricName = rubricRepository.findById(section.getRubricId())
                    .map(Rubric::getRubricName)
                    .orElse(null);
        }

        return new SectionDetail(
                section.getSectionId(),
                section.getSectionName(),
                section.getStartDate(),
                section.getEndDate(),
                section.isActive(),
                section.getRubricId(),
                rubricName,
                List.of(),
                List.of(),
                List.of()
        );
    }
}

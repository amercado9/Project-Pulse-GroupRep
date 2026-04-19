package team.projectpulse.section.service;

import org.springframework.stereotype.Service;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.dto.SectionSummary;
import team.projectpulse.section.repository.SectionRepository;

import java.util.List;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<SectionSummary> findSections(String name) {
        List<Section> sections = (name != null && !name.isBlank())
                ? sectionRepository.findBySectionNameContainingIgnoreCaseOrderBySectionNameDesc(name.trim())
                : sectionRepository.findAllByOrderBySectionNameDesc();

        return sections.stream()
                .map(s -> new SectionSummary(s.getSectionId(), s.getSectionName(), List.of()))
                .toList();
    }

    public Section findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));
    }
}

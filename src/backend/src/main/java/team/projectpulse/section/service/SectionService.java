package team.projectpulse.section.service;

import org.springframework.stereotype.Service;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionAlreadyExistsException;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.dto.CreateSectionRequest;
import team.projectpulse.section.dto.SectionDetail;
import team.projectpulse.section.dto.SetActiveWeeksRequest;
import team.projectpulse.section.dto.UpdateSectionRequest;
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

    public SectionDetail createSection(CreateSectionRequest request) {
        if (sectionRepository.existsBySectionNameIgnoreCase(request.sectionName())) {
            throw new SectionAlreadyExistsException(request.sectionName());
        }

        Section section = new Section();
        section.setSectionName(request.sectionName());
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubricId(request.rubricId());

        Section saved = sectionRepository.save(section);

        String rubricName = null;
        if (saved.getRubricId() != null) {
            rubricName = rubricRepository.findById(saved.getRubricId())
                    .map(Rubric::getRubricName)
                    .orElse(null);
        }

        return new SectionDetail(
                saved.getSectionId(),
                saved.getSectionName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.isActive(),
                saved.getRubricId(),
                rubricName,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public SectionDetail updateSection(Long id, UpdateSectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));

        String newName = request.sectionName();
        if (!section.getSectionName().equalsIgnoreCase(newName)
                && sectionRepository.existsBySectionNameIgnoreCase(newName)) {
            throw new SectionAlreadyExistsException(newName);
        }

        section.setSectionName(newName);
        section.setStartDate(request.startDate());
        section.setEndDate(request.endDate());
        section.setRubricId(request.rubricId());

        Section saved = sectionRepository.save(section);

        String rubricName = null;
        if (saved.getRubricId() != null) {
            rubricName = rubricRepository.findById(saved.getRubricId())
                    .map(Rubric::getRubricName)
                    .orElse(null);
        }

        return new SectionDetail(
                saved.getSectionId(),
                saved.getSectionName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.isActive(),
                saved.getRubricId(),
                rubricName,
                saved.getActiveWeeks(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    public SectionDetail setupActiveWeeks(Long id, SetActiveWeeksRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));

        section.setActiveWeeks(request.activeWeeks());
        Section saved = sectionRepository.save(section);

        String rubricName = null;
        if (saved.getRubricId() != null) {
            rubricName = rubricRepository.findById(saved.getRubricId())
                    .map(Rubric::getRubricName)
                    .orElse(null);
        }

        return new SectionDetail(
                saved.getSectionId(),
                saved.getSectionName(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.isActive(),
                saved.getRubricId(),
                rubricName,
                saved.getActiveWeeks(),
                List.of(),
                List.of(),
                List.of()
        );
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
                section.getActiveWeeks(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}

package team.projectpulse.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public Page<Section> searchByCriteria(SectionSearchCriteria criteria, Pageable pageable) {
        String name = (criteria.sectionName() == null || criteria.sectionName().isBlank())
                ? null : criteria.sectionName().trim();
        return sectionRepository.searchByCriteria(criteria.sectionId(), name, pageable);
    }

    public Section findById(Long id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new SectionNotFoundException(id));
    }

    /**
     * UC-4: Create a new senior design section.
     * Rejects if a section with the same name already exists.
     */
    @Transactional
    public Section createSection(Section section) {
        if (sectionRepository.existsBySectionNameIgnoreCase(section.getSectionName().trim())) {
            throw new SectionAlreadyExistsException(section.getSectionName().trim());
        }
        section.setSectionId(null);
        section.setSectionName(section.getSectionName().trim());
        if (section.getActiveWeeks() == null) section.setActiveWeeks(List.of());
        return sectionRepository.save(section);
    }

    /**
     * UC-6: Compute all Monday–Sunday weeks between section start/end dates,
     * marking each as active if its week number appears in section.activeWeeks.
     */
    public List<SectionWeekInfo> getWeeks(Long sectionId) {
        Section section = findById(sectionId);
        if (section.getStartDate() == null || section.getEndDate() == null) {
            return List.of();
        }
        Set<String> activeSet = new HashSet<>(
                section.getActiveWeeks() != null ? section.getActiveWeeks() : List.of()
        );
        List<SectionWeekInfo> weeks = new ArrayList<>();
        LocalDate weekStart = section.getStartDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int num = 1;
        while (!weekStart.isAfter(section.getEndDate())) {
            LocalDate weekEnd = weekStart.with(DayOfWeek.SUNDAY);
            String n = String.valueOf(num);
            weeks.add(new SectionWeekInfo(n, weekStart.toString(), weekEnd.toString(), activeSet.contains(n)));
            weekStart = weekStart.plusWeeks(1);
            num++;
        }
        return weeks;
    }

    /**
     * UC-6: Replace the section's active weeks with the provided list.
     */
    @Transactional
    public void setUpActiveWeeks(Long sectionId, List<String> activeWeeks) {
        Section section = findById(sectionId);
        section.setActiveWeeks(activeWeeks != null ? activeWeeks : List.of());
        sectionRepository.save(section);
    }
}

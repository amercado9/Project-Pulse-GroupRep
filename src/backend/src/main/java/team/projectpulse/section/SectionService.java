package team.projectpulse.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
}

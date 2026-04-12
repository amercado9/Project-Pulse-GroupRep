package team.projectpulse.rubric;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RubricService {

    private final RubricRepository rubricRepository;
    private final CriterionRepository criterionRepository;

    public RubricService(RubricRepository rubricRepository, CriterionRepository criterionRepository) {
        this.rubricRepository = rubricRepository;
        this.criterionRepository = criterionRepository;
    }

    // ── Criteria ─────────────────────────────────────────────────────────────

    public List<Criterion> searchCriteria(Long criterionId, String criterion) {
        String name = (criterion == null || criterion.isBlank()) ? null : criterion.trim();
        return criterionRepository.searchByCriteria(criterionId, name);
    }

    public Criterion findCriterionById(Long id) {
        return criterionRepository.findById(id)
                .orElseThrow(() -> new RubricNotFoundException("criterion", id));
    }

    public Criterion createCriterion(Criterion c) {
        c.setCriterionId(null);
        return criterionRepository.save(c);
    }

    public Criterion updateCriterion(Long id, Criterion c) {
        Criterion existing = findCriterionById(id);
        existing.setCriterion(c.getCriterion());
        existing.setDescription(c.getDescription());
        existing.setMaxScore(c.getMaxScore());
        existing.setCourseId(c.getCourseId());
        return criterionRepository.save(existing);
    }

    // ── Rubrics ───────────────────────────────────────────────────────────────

    public List<Rubric> searchRubrics(Long rubricId, String rubricName) {
        String name = (rubricName == null || rubricName.isBlank()) ? null : rubricName.trim();
        return rubricRepository.searchByCriteria(rubricId, name);
    }

    public Rubric findRubricById(Long id) {
        return rubricRepository.findById(id)
                .orElseThrow(() -> new RubricNotFoundException("rubric", id));
    }

    public Rubric createRubric(Rubric r) {
        r.setRubricId(null);
        r.setCriteria(List.of());
        return rubricRepository.save(r);
    }

    public Rubric updateRubric(Long id, Rubric r) {
        Rubric existing = findRubricById(id);
        existing.setRubricName(r.getRubricName());
        existing.setCourseId(r.getCourseId());
        return rubricRepository.save(existing);
    }

    public Rubric assignCriterion(Long rubricId, Long criterionId) {
        Rubric rubric = findRubricById(rubricId);
        Criterion criterion = findCriterionById(criterionId);
        if (!rubric.getCriteria().contains(criterion)) {
            rubric.getCriteria().add(criterion);
            rubricRepository.save(rubric);
        }
        return rubric;
    }

    public Rubric unassignCriterion(Long rubricId, Long criterionId) {
        Rubric rubric = findRubricById(rubricId);
        rubric.getCriteria().removeIf(c -> c.getCriterionId().equals(criterionId));
        return rubricRepository.save(rubric);
    }
}

package team.projectpulse.rubric.service;

import org.springframework.stereotype.Service;
import team.projectpulse.rubric.domain.Criterion;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.domain.RubricAlreadyExistsException;
import team.projectpulse.rubric.domain.RubricNotFoundException;
import team.projectpulse.rubric.dto.CreateRubricRequest;
import team.projectpulse.rubric.repository.RubricRepository;

import java.util.List;

@Service
public class RubricService {

    private final RubricRepository rubricRepository;

    public RubricService(RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    public Rubric createRubric(CreateRubricRequest request) {
        if (rubricRepository.existsByRubricNameIgnoreCase(request.rubricName().trim())) {
            throw new RubricAlreadyExistsException(request.rubricName().trim());
        }

        List<Criterion> criteria = request.criteria().stream().map(cr -> {
            Criterion c = new Criterion();
            c.setCriterion(cr.criterion().trim());
            c.setDescription(cr.description() != null ? cr.description().trim() : null);
            c.setMaxScore(cr.maxScore());
            return c;
        }).toList();

        Rubric rubric = new Rubric();
        rubric.setRubricName(request.rubricName().trim());
        rubric.setCriteria(criteria);
        return rubricRepository.save(rubric);
    }

    public List<Rubric> findAllRubrics() {
        return rubricRepository.findAll();
    }

    public Rubric findRubricById(Long id) {
        return rubricRepository.findById(id)
                .orElseThrow(() -> new RubricNotFoundException(id));
    }
}

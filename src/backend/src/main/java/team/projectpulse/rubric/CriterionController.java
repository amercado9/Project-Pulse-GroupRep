package team.projectpulse.rubric;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.common.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/criteria")
public class CriterionController {

    private final RubricService rubricService;

    public CriterionController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    /** POST /criteria/search */
    @PostMapping("/search")
    public Result<Map<String, Object>> search(@RequestBody CriterionSearchRequest body) {
        List<Criterion> list = rubricService.searchCriteria(body.criterionId(), body.criterion());
        return Result.success(Map.of("content", list));
    }

    /** GET /criteria/{id} */
    @GetMapping("/{id}")
    public Result<Criterion> findById(@PathVariable Long id) {
        return Result.success(rubricService.findCriterionById(id));
    }

    /** POST /criteria */
    @PostMapping
    public Result<Criterion> create(@RequestBody Criterion criterion) {
        return Result.success("Criterion created.", rubricService.createCriterion(criterion));
    }

    /** PUT /criteria/{id} */
    @PutMapping("/{id}")
    public Result<Criterion> update(@PathVariable Long id, @RequestBody Criterion criterion) {
        return Result.success("Criterion updated.", rubricService.updateCriterion(id, criterion));
    }

    record CriterionSearchRequest(Long criterionId, String criterion) {}
}

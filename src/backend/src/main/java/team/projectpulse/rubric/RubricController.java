package team.projectpulse.rubric;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.common.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    /** POST /rubrics/search */
    @PostMapping("/search")
    public Result<Map<String, Object>> search(@RequestBody RubricSearchRequest body) {
        List<Rubric> list = rubricService.searchRubrics(body.rubricId(), body.rubricName());
        return Result.success(Map.of("content", list));
    }

    /** GET /rubrics/{id} */
    @GetMapping("/{id}")
    public Result<Rubric> findById(@PathVariable Long id) {
        return Result.success(rubricService.findRubricById(id));
    }

    /** POST /rubrics */
    @PostMapping
    public Result<Rubric> create(@RequestBody Rubric rubric) {
        return Result.success("Rubric created.", rubricService.createRubric(rubric));
    }

    /** PUT /rubrics/{id} */
    @PutMapping("/{id}")
    public Result<Rubric> update(@PathVariable Long id, @RequestBody Rubric rubric) {
        return Result.success("Rubric updated.", rubricService.updateRubric(id, rubric));
    }

    /** PUT /rubrics/{rubricId}/criteria/{criterionId} */
    @PutMapping("/{rubricId}/criteria/{criterionId}")
    public Result<Rubric> assignCriterion(@PathVariable Long rubricId, @PathVariable Long criterionId) {
        return Result.success("Criterion assigned.", rubricService.assignCriterion(rubricId, criterionId));
    }

    /** DELETE /rubrics/{rubricId}/criteria/{criterionId} */
    @DeleteMapping("/{rubricId}/criteria/{criterionId}")
    public Result<Rubric> unassignCriterion(@PathVariable Long rubricId, @PathVariable Long criterionId) {
        return Result.success("Criterion removed.", rubricService.unassignCriterion(rubricId, criterionId));
    }

    record RubricSearchRequest(Long rubricId, String rubricName) {}
}

package team.projectpulse.rubric.controller;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.dto.CreateRubricRequest;
import team.projectpulse.rubric.service.RubricService;
import team.projectpulse.system.Result;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/rubrics")
public class RubricController {

    private final RubricService rubricService;

    public RubricController(RubricService rubricService) {
        this.rubricService = rubricService;
    }

    @PostMapping
    public Result<Rubric> createRubric(@RequestBody CreateRubricRequest request) {
        Rubric created = rubricService.createRubric(request);
        return Result.success("Rubric created successfully.", created);
    }

    @GetMapping
    public Result<List<Rubric>> getAllRubrics() {
        return Result.success(rubricService.findAllRubrics());
    }

    @GetMapping("/{id}")
    public Result<Rubric> getRubricById(@PathVariable Long id) {
        return Result.success(rubricService.findRubricById(id));
    }
}

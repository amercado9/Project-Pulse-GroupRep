package team.projectpulse.section.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.section.dto.CreateSectionRequest;
import team.projectpulse.section.dto.SectionDetail;
import team.projectpulse.section.dto.SetActiveWeeksRequest;
import team.projectpulse.section.dto.UpdateSectionRequest;
import team.projectpulse.section.dto.SectionSummary;
import team.projectpulse.section.service.SectionService;
import team.projectpulse.system.Result;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<SectionDetail> createSection(@RequestBody CreateSectionRequest request) {
        return Result.success(sectionService.createSection(request));
    }

    @PutMapping("/{id}")
    public Result<SectionDetail> updateSection(@PathVariable Long id, @RequestBody UpdateSectionRequest request) {
        return Result.success(sectionService.updateSection(id, request));
    }

    @PutMapping("/{id}/active-weeks")
    public Result<SectionDetail> setupActiveWeeks(@PathVariable Long id, @RequestBody SetActiveWeeksRequest request) {
        return Result.success(sectionService.setupActiveWeeks(id, request));
    }

    @GetMapping
    public Result<List<SectionSummary>> findSections(@RequestParam(required = false) String name) {
        return Result.success(sectionService.findSections(name));
    }

    @GetMapping("/{id}")
    public Result<SectionDetail> getSection(@PathVariable Long id) {
        return Result.success(sectionService.findSectionDetail(id));
    }
}

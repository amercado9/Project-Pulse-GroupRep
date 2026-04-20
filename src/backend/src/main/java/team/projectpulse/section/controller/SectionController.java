package team.projectpulse.section.controller;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.section.dto.SectionDetail;
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

    @GetMapping
    public Result<List<SectionSummary>> findSections(@RequestParam(required = false) String name) {
        return Result.success(sectionService.findSections(name));
    }

    @GetMapping("/{id}")
    public Result<SectionDetail> getSection(@PathVariable Long id) {
        return Result.success(sectionService.findSectionDetail(id));
    }
}

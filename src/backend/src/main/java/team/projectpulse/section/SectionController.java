package team.projectpulse.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.common.Result;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    /**
     * UC-2: Admin finds senior design sections.
     * POST /api/v1/sections/search?page=0&size=10
     */
    @PostMapping("/search")
    public Result<Map<String, Object>> searchSections(
            @RequestBody SectionSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sectionName"));
        Page<Section> result = sectionService.searchByCriteria(criteria, pageable);

        Map<String, Object> data = Map.of(
                "content", result.getContent(),
                "totalElements", result.getTotalElements(),
                "totalPages", result.getTotalPages()
        );
        return Result.success("Sections retrieved.", data);
    }

    /**
     * UC-3: Admin views a senior design section.
     * GET /api/v1/sections/{id}
     */
    @GetMapping("/{id}")
    public Result<Section> findById(@PathVariable Long id) {
        return Result.success(sectionService.findById(id));
    }

    /**
     * UC-6: Get all computed weeks for a section (Monday–Sunday ranges).
     * GET /api/v1/sections/{id}/weeks
     */
    @GetMapping("/{id}/weeks")
    public Result<List<SectionWeekInfo>> getWeeks(@PathVariable Long id) {
        return Result.success(sectionService.getWeeks(id));
    }

    /**
     * UC-6: Save the admin's chosen active weeks for a section.
     * POST /api/v1/sections/{id}/weeks
     */
    @PostMapping("/{id}/weeks")
    public Result<Void> setUpActiveWeeks(@PathVariable Long id, @RequestBody List<String> activeWeeks) {
        sectionService.setUpActiveWeeks(id, activeWeeks);
        return Result.success("Active weeks updated.", null);
    }
}

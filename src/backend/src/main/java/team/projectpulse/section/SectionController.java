package team.projectpulse.section;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.common.Result;

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
}

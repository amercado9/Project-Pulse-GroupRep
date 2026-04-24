package team.projectpulse.report.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.report.dto.SectionPeerEvalReportResponse;
import team.projectpulse.report.service.ReportService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/sections/{sectionId}")
public class SectionReportController {

    private final ReportService reportService;

    public SectionReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/peer-eval-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public Result<SectionPeerEvalReportResponse> getSectionPeerEvalReport(
            @PathVariable Long sectionId,
            @RequestParam String week
    ) {
        return Result.success(reportService.generateSectionPeerEvalReport(sectionId, week));
    }
}

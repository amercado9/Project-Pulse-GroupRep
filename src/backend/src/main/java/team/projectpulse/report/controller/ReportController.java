package team.projectpulse.report.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.report.dto.TeamWarReportResponse;
import team.projectpulse.report.service.ReportService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/teams/{teamId}/war-report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public Result<TeamWarReportResponse> getTeamWarReport(
            @PathVariable Long teamId,
            @RequestParam String week
    ) {
        return Result.success(reportService.generateTeamWarReport(teamId, week));
    }
}

package team.projectpulse.report.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.report.dto.StudentWarReportResponse;
import team.projectpulse.report.dto.TeamWarReportResponse;
import team.projectpulse.report.service.ReportService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/teams/{teamId}")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR', 'STUDENT')")
    public Result<TeamWarReportResponse> getTeamWarReport(
            @PathVariable Long teamId,
            @RequestParam String week
    ) {
        return Result.success(reportService.generateTeamWarReport(teamId, week));
    }

    @GetMapping("/students/{studentId}/war-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public Result<StudentWarReportResponse> getStudentWarReport(
            @PathVariable Long teamId,
            @PathVariable Long studentId,
            @RequestParam String startWeek,
            @RequestParam String endWeek
    ) {
        return Result.success(reportService.generateStudentWarReport(teamId, studentId, startWeek, endWeek));
    }
}

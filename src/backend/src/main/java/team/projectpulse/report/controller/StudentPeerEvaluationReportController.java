package team.projectpulse.report.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.report.dto.StudentPeerEvaluationReportResponse;
import team.projectpulse.report.service.ReportService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/reports/peer-evaluations/me")
public class StudentPeerEvaluationReportController {

    private final ReportService reportService;

    public StudentPeerEvaluationReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<StudentPeerEvaluationReportResponse> getOwnPeerEvaluationReport(
        Authentication authentication,
        @RequestParam(required = false) String week
    ) {
        return Result.success(reportService.generateOwnPeerEvaluationReport(authentication.getName(), week));
    }
}

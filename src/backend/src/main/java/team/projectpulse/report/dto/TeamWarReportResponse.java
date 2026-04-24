package team.projectpulse.report.dto;

import java.util.List;

public record TeamWarReportResponse(
    Long teamId,
    String teamName,
    String week,
    List<StudentWarReport> studentReports
) {}

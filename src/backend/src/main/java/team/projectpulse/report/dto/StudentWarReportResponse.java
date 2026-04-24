package team.projectpulse.report.dto;

import java.util.List;

public record StudentWarReportResponse(
    Long studentId,
    String studentName,
    String startWeek,
    String endWeek,
    List<StudentWeekWarReport> weekReports
) {}

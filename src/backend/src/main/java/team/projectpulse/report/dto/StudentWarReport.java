package team.projectpulse.report.dto;

import java.util.List;

public record StudentWarReport(
    Long studentId,
    String studentName,
    boolean submitted,
    List<WarReportRow> activities
) {}

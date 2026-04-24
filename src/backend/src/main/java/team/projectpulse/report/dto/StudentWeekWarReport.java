package team.projectpulse.report.dto;

import java.util.List;

public record StudentWeekWarReport(
    String week,
    List<WarReportRow> activities
) {}

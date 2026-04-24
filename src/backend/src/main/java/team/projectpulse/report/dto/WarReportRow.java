package team.projectpulse.report.dto;

import java.math.BigDecimal;

public record WarReportRow(
    String activityCategory,
    String plannedActivity,
    String description,
    BigDecimal plannedHours,
    BigDecimal actualHours,
    String status
) {}

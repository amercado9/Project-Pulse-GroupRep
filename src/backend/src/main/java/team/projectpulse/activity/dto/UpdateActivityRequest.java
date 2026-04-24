package team.projectpulse.activity.dto;

import team.projectpulse.activity.domain.ActivityCategory;
import team.projectpulse.activity.domain.ActivityStatus;

import java.math.BigDecimal;

public record UpdateActivityRequest(
    ActivityCategory category,
    String plannedActivity,
    String description,
    BigDecimal plannedHours,
    BigDecimal actualHours,
    ActivityStatus status
) {
}

package team.projectpulse.activity.dto;

import java.util.List;

public record ActivityWorkspace(
    Long sectionId,
    String sectionName,
    Long teamId,
    String teamName,
    String selectedWeek,
    List<ActivityWeekOption> availableWeeks,
    List<ActivityDetail> activities,
    boolean canManageActivities,
    String availabilityMessage
) {
}

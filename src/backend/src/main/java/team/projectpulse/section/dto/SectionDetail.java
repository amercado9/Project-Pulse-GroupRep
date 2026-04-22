package team.projectpulse.section.dto;

import java.time.LocalDate;
import java.util.List;

public record SectionDetail(
        Long sectionId,
        String sectionName,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        Long rubricId,
        String rubricName,
        List<String> activeWeeks,
        List<TeamInfo> teams,
        List<String> unassignedStudents,
        List<String> unassignedInstructors
) {
    public record TeamInfo(Long teamId, String teamName, List<String> members, List<String> instructors) {}
}

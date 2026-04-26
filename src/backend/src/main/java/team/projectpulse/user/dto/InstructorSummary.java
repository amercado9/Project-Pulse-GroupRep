package team.projectpulse.user.dto;

import java.util.List;

public record InstructorSummary(
        Long instructorId,
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        List<String> teamNames,
        Integer academicYear
) {}

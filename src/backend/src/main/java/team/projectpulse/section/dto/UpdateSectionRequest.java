package team.projectpulse.section.dto;

import java.time.LocalDate;

public record UpdateSectionRequest(
        String sectionName,
        LocalDate startDate,
        LocalDate endDate,
        Long rubricId
) {}

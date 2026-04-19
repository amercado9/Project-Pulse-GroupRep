package team.projectpulse.section.dto;

import java.util.List;

public record SectionSummary(Long sectionId, String sectionName, List<String> teamNames) {}

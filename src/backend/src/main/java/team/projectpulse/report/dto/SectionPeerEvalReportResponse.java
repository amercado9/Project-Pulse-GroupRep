package team.projectpulse.report.dto;

import java.util.List;

public record SectionPeerEvalReportResponse(
        Long sectionId,
        String sectionName,
        String week,
        List<StudentPeerEvalReport> studentReports
) {}

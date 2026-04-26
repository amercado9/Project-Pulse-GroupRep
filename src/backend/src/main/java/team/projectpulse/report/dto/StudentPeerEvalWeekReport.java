package team.projectpulse.report.dto;

import java.util.List;

public record StudentPeerEvalWeekReport(
        String week,
        Double grade,
        Double maxGrade,
        List<EvalReportEntry> evaluations
) {}

package team.projectpulse.report.dto;

import java.util.List;

public record StudentPeerEvalReport(
        Long studentId,
        String studentName,
        String teamName,
        boolean submitted,
        Double grade,
        Double maxGrade,
        List<EvalReportEntry> evaluations
) {}

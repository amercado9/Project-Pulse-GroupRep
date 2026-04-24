package team.projectpulse.report.dto;

public record EvalReportEntry(
        String evaluatorName,
        double totalScore,
        double maxScore,
        String publicComment,
        String privateComment
) {}

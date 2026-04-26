package team.projectpulse.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record StudentPeerEvaluationReportResponse(
    Long sectionId,
    String sectionName,
    Long teamId,
    String teamName,
    Long studentId,
    String studentName,
    String selectedWeek,
    String selectedWeekLabel,
    String selectedWeekRangeLabel,
    List<ReportWeekOption> availableWeeks,
    boolean reportAvailable,
    String availabilityMessage,
    List<StudentPeerEvaluationCriterionAverageDto> criterionAverages,
    List<String> publicComments,
    BigDecimal overallGrade,
    Integer maxTotalScore
) {
}

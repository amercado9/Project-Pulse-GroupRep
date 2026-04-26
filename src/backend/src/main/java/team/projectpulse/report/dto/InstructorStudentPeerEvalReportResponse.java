package team.projectpulse.report.dto;

import java.util.List;

public record InstructorStudentPeerEvalReportResponse(
        Long studentId,
        String studentName,
        Long teamId,
        String teamName,
        String startWeek,
        String endWeek,
        List<StudentPeerEvalWeekReport> weekReports
) {}

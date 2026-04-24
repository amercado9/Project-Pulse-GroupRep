package team.projectpulse.report.service;

import org.springframework.stereotype.Service;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.repository.EvaluationSubmissionRepository;
import team.projectpulse.report.dto.EvalReportEntry;
import team.projectpulse.report.dto.SectionPeerEvalReportResponse;
import team.projectpulse.report.dto.StudentPeerEvalReport;
import team.projectpulse.report.dto.StudentWarReport;
import team.projectpulse.report.dto.StudentWarReportResponse;
import team.projectpulse.report.dto.StudentWeekWarReport;
import team.projectpulse.report.dto.TeamWarReportResponse;
import team.projectpulse.report.dto.WarReportRow;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TeamRepository teamRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final EvaluationSubmissionRepository evaluationSubmissionRepository;

    public ReportService(TeamRepository teamRepository, ActivityRepository activityRepository,
                         UserRepository userRepository, SectionRepository sectionRepository,
                         EvaluationSubmissionRepository evaluationSubmissionRepository) {
        this.teamRepository = teamRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.evaluationSubmissionRepository = evaluationSubmissionRepository;
    }

    public TeamWarReportResponse generateTeamWarReport(Long teamId, String week) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new InvalidTeamException("Team not found with id " + teamId));

        List<Activity> activities = activityRepository.findByTeamIdAndWeek(teamId, week);

        Map<Long, List<Activity>> byStudent = activities.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        List<User> students = new ArrayList<>(team.getStudents());
        students.sort((a, b) -> {
            int cmp = a.getLastName().compareToIgnoreCase(b.getLastName());
            return cmp != 0 ? cmp : a.getFirstName().compareToIgnoreCase(b.getFirstName());
        });

        List<StudentWarReport> studentReports = students.stream()
                .map(student -> {
                    List<Activity> studentActivities = byStudent.getOrDefault(student.getId(), List.of());
                    boolean submitted = !studentActivities.isEmpty();
                    List<WarReportRow> rows = studentActivities.stream()
                            .map(a -> new WarReportRow(
                                    a.getCategory().name(),
                                    a.getPlannedActivity(),
                                    a.getDescription(),
                                    a.getPlannedHours(),
                                    a.getActualHours(),
                                    a.getStatus().name()
                            ))
                            .toList();
                    return new StudentWarReport(
                            student.getId(),
                            student.getFirstName() + " " + student.getLastName(),
                            submitted,
                            rows
                    );
                })
                .toList();

        return new TeamWarReportResponse(team.getTeamId(), team.getTeamName(), week, studentReports);
    }

    public StudentWarReportResponse generateStudentWarReport(Long teamId, Long studentId, String startWeek, String endWeek) {
        teamRepository.findById(teamId)
                .orElseThrow(() -> new InvalidTeamException("Team not found with id " + teamId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new InvalidTeamException("Student not found with id " + studentId));

        List<Activity> activities = activityRepository
                .findByStudentIdAndTeamIdAndWeekRange(studentId, teamId, startWeek, endWeek);

        Map<String, List<Activity>> byWeek = activities.stream()
                .collect(Collectors.groupingBy(Activity::getWeek, LinkedHashMap::new, Collectors.toList()));

        List<StudentWeekWarReport> weekReports = byWeek.entrySet().stream()
                .map(entry -> {
                    List<WarReportRow> rows = entry.getValue().stream()
                            .map(a -> new WarReportRow(
                                    a.getCategory().name(),
                                    a.getPlannedActivity(),
                                    a.getDescription(),
                                    a.getPlannedHours(),
                                    a.getActualHours(),
                                    a.getStatus().name()
                            ))
                            .toList();
                    return new StudentWeekWarReport(entry.getKey(), rows);
                })
                .toList();

        return new StudentWarReportResponse(
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                startWeek,
                endWeek,
                weekReports
        );
    }

    public SectionPeerEvalReportResponse generateSectionPeerEvalReport(Long sectionId, String week) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new InvalidTeamException("Section not found with id " + sectionId));

        List<Team> teams = teamRepository.findAllBySectionIdWithStudentsOrdered(sectionId);

        List<User> allStudents = teams.stream()
                .flatMap(t -> t.getStudents().stream())
                .distinct()
                .sorted((a, b) -> {
                    int cmp = a.getLastName().compareToIgnoreCase(b.getLastName());
                    return cmp != 0 ? cmp : a.getFirstName().compareToIgnoreCase(b.getFirstName());
                })
                .toList();

        Map<Long, String> studentTeamNames = new java.util.HashMap<>();
        for (Team team : teams) {
            for (User student : team.getStudents()) {
                studentTeamNames.put(student.getId(), team.getTeamName());
            }
        }

        List<EvaluationEntry> entries = evaluationSubmissionRepository
                .findEntriesWithScoresBySectionIdAndWeek(sectionId, week);

        Set<Long> submitterIds = new java.util.HashSet<>(
                evaluationSubmissionRepository.findSubmitterIdsBySectionIdAndWeek(sectionId, week)
        );

        Map<Long, List<EvaluationEntry>> entriesByEvaluatee = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getEvaluateeStudent().getId()));

        List<StudentPeerEvalReport> studentReports = allStudents.stream()
                .map(student -> {
                    List<EvaluationEntry> received = entriesByEvaluatee.getOrDefault(student.getId(), List.of());
                    boolean submitted = submitterIds.contains(student.getId());
                    String teamName = studentTeamNames.getOrDefault(student.getId(), "—");

                    List<EvalReportEntry> evalRows = received.stream()
                            .map(entry -> {
                                double totalScore = entry.getScores().stream()
                                        .mapToInt(EvaluationScore::getScore).sum();
                                double maxScore = entry.getScores().stream()
                                        .mapToDouble(s -> s.getCriterion().getMaxScore()).sum();
                                String evaluatorName = entry.getSubmission().getEvaluatorStudent().getFirstName()
                                        + " " + entry.getSubmission().getEvaluatorStudent().getLastName();
                                return new EvalReportEntry(evaluatorName, totalScore, maxScore,
                                        entry.getPublicComment(), entry.getPrivateComment());
                            })
                            .toList();

                    Double grade = received.isEmpty() ? null
                            : evalRows.stream().mapToDouble(EvalReportEntry::totalScore).average().orElse(0.0);
                    Double maxGrade = received.isEmpty() ? null : evalRows.get(0).maxScore();

                    return new StudentPeerEvalReport(
                            student.getId(),
                            student.getFirstName() + " " + student.getLastName(),
                            teamName,
                            submitted,
                            grade,
                            maxGrade,
                            evalRows
                    );
                })
                .toList();

        return new SectionPeerEvalReportResponse(sectionId, section.getSectionName(), week, studentReports);
    }
}

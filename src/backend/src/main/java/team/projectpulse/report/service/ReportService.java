package team.projectpulse.report.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.repository.EvaluationSubmissionRepository;
import team.projectpulse.report.domain.InvalidPeerEvaluationReportException;
import team.projectpulse.report.dto.EvalReportEntry;
import team.projectpulse.report.dto.ReportWeekOption;
import team.projectpulse.report.dto.SectionPeerEvalReportResponse;
import team.projectpulse.report.dto.StudentPeerEvalReport;
import team.projectpulse.report.dto.StudentPeerEvaluationCriterionAverageDto;
import team.projectpulse.report.dto.StudentPeerEvaluationReportResponse;
import team.projectpulse.report.dto.StudentWarReport;
import team.projectpulse.report.dto.StudentWarReportResponse;
import team.projectpulse.report.dto.StudentWeekWarReport;
import team.projectpulse.report.dto.TeamWarReportResponse;
import team.projectpulse.report.dto.WarReportRow;
import team.projectpulse.rubric.domain.Criterion;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.system.IsoWeekUtils;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TeamRepository teamRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final EvaluationSubmissionRepository evaluationSubmissionRepository;
    private final Clock clock;

    @Autowired
    public ReportService(
        TeamRepository teamRepository,
        ActivityRepository activityRepository,
        UserRepository userRepository,
        SectionRepository sectionRepository,
        EvaluationSubmissionRepository evaluationSubmissionRepository
    ) {
        this(
            teamRepository,
            activityRepository,
            userRepository,
            sectionRepository,
            evaluationSubmissionRepository,
            Clock.systemDefaultZone()
        );
    }

    ReportService(
        TeamRepository teamRepository,
        ActivityRepository activityRepository,
        UserRepository userRepository,
        SectionRepository sectionRepository,
        EvaluationSubmissionRepository evaluationSubmissionRepository,
        Clock clock
    ) {
        this.teamRepository = teamRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.evaluationSubmissionRepository = evaluationSubmissionRepository;
        this.clock = clock;
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
            .flatMap(team -> team.getStudents().stream())
            .distinct()
            .sorted((a, b) -> {
                int cmp = a.getLastName().compareToIgnoreCase(b.getLastName());
                return cmp != 0 ? cmp : a.getFirstName().compareToIgnoreCase(b.getFirstName());
            })
            .toList();

        Map<Long, String> studentTeamNames = new LinkedHashMap<>();
        for (Team team : teams) {
            for (User student : team.getStudents()) {
                studentTeamNames.put(student.getId(), team.getTeamName());
            }
        }

        List<EvaluationEntry> entries = evaluationSubmissionRepository
            .findEntriesWithScoresBySectionIdAndWeek(sectionId, week);

        Set<Long> submitterIds = Set.copyOf(
            evaluationSubmissionRepository.findSubmitterIdsBySectionIdAndWeek(sectionId, week)
        );

        Map<Long, List<EvaluationEntry>> entriesByEvaluatee = entries.stream()
            .collect(Collectors.groupingBy(entry -> entry.getEvaluateeStudent().getId()));

        List<StudentPeerEvalReport> studentReports = allStudents.stream()
            .map(student -> {
                List<EvaluationEntry> received = entriesByEvaluatee.getOrDefault(student.getId(), List.of());
                boolean submitted = submitterIds.contains(student.getId());
                String teamName = studentTeamNames.getOrDefault(student.getId(), "—");

                List<EvalReportEntry> evalRows = received.stream()
                    .map(entry -> {
                        double totalScore = entry.getScores().stream().mapToInt(EvaluationScore::getScore).sum();
                        double maxScore = entry.getScores().stream()
                            .mapToDouble(score -> score.getCriterion().getMaxScore())
                            .sum();
                        String evaluatorName = entry.getSubmission().getEvaluatorStudent().getFirstName()
                            + " "
                            + entry.getSubmission().getEvaluatorStudent().getLastName();
                        return new EvalReportEntry(
                            evaluatorName,
                            totalScore,
                            maxScore,
                            entry.getPublicComment(),
                            entry.getPrivateComment()
                        );
                    })
                    .toList();

                Double grade = received.isEmpty()
                    ? null
                    : evalRows.stream().mapToDouble(EvalReportEntry::totalScore).average().orElse(0.0);
                Double maxGrade = received.isEmpty() ? null : evalRows.getFirst().maxScore();

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

    public StudentPeerEvaluationReportResponse generateOwnPeerEvaluationReport(String studentEmail, String requestedWeek) {
        User student = requireStudent(studentEmail);
        Section section = student.getSection();
        if (section == null) {
            throw new InvalidPeerEvaluationReportException("You must belong to a senior design section to view your peer evaluation report.");
        }

        List<String> availableWeeks = deriveAvailableWeeks(section);
        if (availableWeeks.isEmpty()) {
            throw new InvalidPeerEvaluationReportException("No active peer evaluation report weeks are available.");
        }

        String selectedWeek = resolveSelectedWeek(availableWeeks, requestedWeek);
        List<ReportWeekOption> weekOptions = availableWeeks.stream()
            .map(week -> new ReportWeekOption(
                week,
                IsoWeekUtils.formatWeekLabel(week),
                IsoWeekUtils.formatWeekRangeLabel(week)
            ))
            .toList();

        List<EvaluationEntry> teammateEntries = evaluationSubmissionRepository
            .findEntriesByEvaluateeStudentIdAndWeek(student.getId(), selectedWeek)
            .stream()
            .filter(entry -> !Objects.equals(entry.getSubmission().getEvaluatorStudent().getId(), student.getId()))
            .sorted(Comparator
                .comparing((EvaluationEntry entry) -> entry.getSubmission().getEvaluatorStudent().getLastName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.getSubmission().getEvaluatorStudent().getFirstName(), String.CASE_INSENSITIVE_ORDER))
            .toList();

        if (teammateEntries.isEmpty()) {
            return new StudentPeerEvaluationReportResponse(
                section.getSectionId(),
                section.getSectionName(),
                null,
                null,
                student.getId(),
                student.getFirstName() + " " + student.getLastName(),
                selectedWeek,
                IsoWeekUtils.formatWeekLabel(selectedWeek),
                IsoWeekUtils.formatWeekRangeLabel(selectedWeek),
                weekOptions,
                false,
                "No peer evaluation data is available for the selected week.",
                List.of(),
                List.of(),
                null,
                null
            );
        }

        Team team = teammateEntries.getFirst().getSubmission().getTeam();
        List<StudentPeerEvaluationCriterionAverageDto> criterionAverages = buildCriterionAverages(teammateEntries);
        List<String> publicComments = teammateEntries.stream()
            .map(EvaluationEntry::getPublicComment)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(comment -> !comment.isEmpty())
            .toList();
        BigDecimal overallGrade = buildOverallGrade(teammateEntries);
        Integer maxTotalScore = criterionAverages.isEmpty()
            ? null
            : criterionAverages.stream().mapToInt(average -> (int) average.maxScore()).sum();

        return new StudentPeerEvaluationReportResponse(
            section.getSectionId(),
            section.getSectionName(),
            team.getTeamId(),
            team.getTeamName(),
            student.getId(),
            student.getFirstName() + " " + student.getLastName(),
            selectedWeek,
            IsoWeekUtils.formatWeekLabel(selectedWeek),
            IsoWeekUtils.formatWeekRangeLabel(selectedWeek),
            weekOptions,
            true,
            null,
            criterionAverages,
            publicComments,
            overallGrade,
            maxTotalScore
        );
    }

    private List<StudentPeerEvaluationCriterionAverageDto> buildCriterionAverages(List<EvaluationEntry> entries) {
        Map<Long, CriterionAggregate> aggregates = new LinkedHashMap<>();
        for (EvaluationEntry entry : entries) {
            for (EvaluationScore score : entry.getScores()) {
                Criterion criterion = score.getCriterion();
                aggregates.computeIfAbsent(
                    criterion.getCriterionId(),
                    ignored -> new CriterionAggregate(criterion)
                ).add(score.getScore());
            }
        }

        return aggregates.values().stream()
            .sorted(Comparator.comparing(aggregate -> aggregate.criterion().getCriterionId()))
            .map(aggregate -> new StudentPeerEvaluationCriterionAverageDto(
                aggregate.criterion().getCriterionId(),
                aggregate.criterion().getCriterion(),
                aggregate.criterion().getDescription(),
                average(aggregate.total(), aggregate.count()),
                aggregate.criterion().getMaxScore()
            ))
            .toList();
    }

    private BigDecimal buildOverallGrade(List<EvaluationEntry> entries) {
        BigDecimal total = entries.stream()
            .map(entry -> entry.getScores().stream()
                .map(EvaluationScore::getScore)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return average(total, entries.size());
    }

    private BigDecimal average(BigDecimal total, int count) {
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private User requireStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new InvalidPeerEvaluationReportException("No student account found for the current user."));
        if (!isStudent(student)) {
            throw new InvalidPeerEvaluationReportException("Only students can view their peer evaluation report.");
        }
        return student;
    }

    private boolean isStudent(User user) {
        String roles = user.getRoles() == null ? "" : user.getRoles().toLowerCase();
        return List.of(roles.split("\\s+")).contains("student");
    }

    private List<String> deriveAvailableWeeks(Section section) {
        String currentWeek = IsoWeekUtils.toIsoWeek(LocalDate.now(clock));
        List<String> sectionWeeks = deriveSectionWeeks(section);
        return sectionWeeks.stream()
            .filter(week -> section.getActiveWeeks() != null && section.getActiveWeeks().contains(week))
            .filter(week -> IsoWeekUtils.parseIsoWeekStart(week).isBefore(IsoWeekUtils.parseIsoWeekStart(currentWeek)))
            .sorted(IsoWeekUtils.descendingComparator())
            .toList();
    }

    private String resolveSelectedWeek(List<String> availableWeeks, String requestedWeek) {
        if (requestedWeek != null && !requestedWeek.isBlank()) {
            if (!availableWeeks.contains(requestedWeek)) {
                throw new InvalidPeerEvaluationReportException("The selected week is not available for your peer evaluation report.");
            }
            return requestedWeek;
        }

        String previousWeek = IsoWeekUtils.toIsoWeek(LocalDate.now(clock).minusWeeks(1));
        if (availableWeeks.contains(previousWeek)) {
            return previousWeek;
        }
        return availableWeeks.getFirst();
    }

    private List<String> deriveSectionWeeks(Section section) {
        if (section.getStartDate() == null || section.getEndDate() == null) {
            return List.of();
        }

        LocalDate cursor = IsoWeekUtils.toMonday(section.getStartDate());
        LocalDate end = IsoWeekUtils.toMonday(section.getEndDate());
        List<String> weeks = new ArrayList<>();
        while (!cursor.isAfter(end)) {
            weeks.add(IsoWeekUtils.toIsoWeek(cursor));
            cursor = cursor.plusWeeks(1);
        }
        return weeks;
    }

    private static final class CriterionAggregate {
        private final Criterion criterion;
        private BigDecimal total = BigDecimal.ZERO;
        private int count = 0;

        private CriterionAggregate(Criterion criterion) {
            this.criterion = criterion;
        }

        private void add(Integer score) {
            total = total.add(BigDecimal.valueOf(score));
            count++;
        }

        private Criterion criterion() {
            return criterion;
        }

        private BigDecimal total() {
            return total;
        }

        private int count() {
            return count;
        }
    }

    public team.projectpulse.report.dto.InstructorStudentPeerEvalReportResponse generateInstructorStudentPeerEvalReport(
            Long teamId, Long studentId, String startWeek, String endWeek) {
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new InvalidTeamException("Team not found with id " + teamId));

        User student = userRepository.findById(studentId)
            .orElseThrow(() -> new InvalidTeamException("Student not found with id " + studentId));

        List<EvaluationEntry> allEntries = evaluationSubmissionRepository
            .findEntriesByEvaluateeStudentIdAndTeamIdAndWeekRange(studentId, teamId, startWeek, endWeek);

        Map<String, List<EvaluationEntry>> byWeek = allEntries.stream()
            .collect(Collectors.groupingBy(
                e -> e.getSubmission().getWeek(),
                LinkedHashMap::new,
                Collectors.toList()
            ));

        List<team.projectpulse.report.dto.StudentPeerEvalWeekReport> weekReports = byWeek.entrySet().stream()
            .map(entry -> {
                List<EvalReportEntry> evalRows = entry.getValue().stream()
                    .map(e -> {
                        double totalScore = e.getScores().stream().mapToInt(EvaluationScore::getScore).sum();
                        double maxScore = e.getScores().stream()
                            .mapToDouble(s -> s.getCriterion().getMaxScore()).sum();
                        String evaluatorName = e.getSubmission().getEvaluatorStudent().getFirstName()
                            + " " + e.getSubmission().getEvaluatorStudent().getLastName();
                        return new EvalReportEntry(evaluatorName, totalScore, maxScore,
                            e.getPublicComment(), e.getPrivateComment());
                    })
                    .toList();

                Double grade = evalRows.stream().mapToDouble(EvalReportEntry::totalScore).average().orElse(0.0);
                Double maxGrade = evalRows.isEmpty() ? null : evalRows.getFirst().maxScore();

                return new team.projectpulse.report.dto.StudentPeerEvalWeekReport(entry.getKey(), grade, maxGrade, evalRows);
            })
            .toList();

        return new team.projectpulse.report.dto.InstructorStudentPeerEvalReportResponse(
            student.getId(),
            student.getFirstName() + " " + student.getLastName(),
            team.getTeamId(),
            team.getTeamName(),
            startWeek,
            endWeek,
            weekReports
        );
    }
}

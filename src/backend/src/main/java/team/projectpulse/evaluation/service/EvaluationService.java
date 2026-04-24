package team.projectpulse.evaluation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.domain.EvaluationSubmission;
import team.projectpulse.evaluation.domain.InvalidPeerEvaluationException;
import team.projectpulse.evaluation.domain.PeerEvaluationSubmissionNotFoundException;
import team.projectpulse.evaluation.dto.EvaluationCriterionDto;
import team.projectpulse.evaluation.dto.EvaluationCriterionScoreDraftDto;
import team.projectpulse.evaluation.dto.EvaluationMemberDraftDto;
import team.projectpulse.evaluation.dto.EvaluationWorkspace;
import team.projectpulse.evaluation.dto.PeerEvaluationCriterionScoreSubmission;
import team.projectpulse.evaluation.dto.PeerEvaluationMemberSubmission;
import team.projectpulse.evaluation.dto.PeerEvaluationSubmissionDetail;
import team.projectpulse.evaluation.dto.PeerEvaluationSubmissionRequest;
import team.projectpulse.evaluation.repository.EvaluationSubmissionRepository;
import team.projectpulse.rubric.domain.Criterion;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.rubric.repository.RubricRepository;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class EvaluationService {

    private static final int MAX_COMMENT_LENGTH = 2000;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final WeekFields ISO = WeekFields.ISO;

    private final EvaluationSubmissionRepository evaluationSubmissionRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final RubricRepository rubricRepository;
    private final Clock clock;

    @Autowired
    public EvaluationService(
        EvaluationSubmissionRepository evaluationSubmissionRepository,
        UserRepository userRepository,
        TeamRepository teamRepository,
        RubricRepository rubricRepository
    ) {
        this(evaluationSubmissionRepository, userRepository, teamRepository, rubricRepository, Clock.systemDefaultZone());
    }

    EvaluationService(
        EvaluationSubmissionRepository evaluationSubmissionRepository,
        UserRepository userRepository,
        TeamRepository teamRepository,
        RubricRepository rubricRepository,
        Clock clock
    ) {
        this.evaluationSubmissionRepository = evaluationSubmissionRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.rubricRepository = rubricRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public EvaluationWorkspace loadWorkspace(String studentEmail) {
        User student = requireStudent(studentEmail);
        Section section = student.getSection();
        if (section == null) {
            return unavailableWorkspace(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "You must belong to a senior design section before submitting peer evaluations."
            );
        }

        Team baseTeam = teamRepository.findByStudentIdWithSection(student.getId())
            .orElse(null);
        if (baseTeam == null) {
            return unavailableWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                null,
                null,
                null,
                null,
                null,
                "You must be assigned to a senior design team before submitting peer evaluations."
            );
        }

        Team team = teamRepository.findDetailById(baseTeam.getTeamId())
            .orElse(baseTeam);

        LocalDate today = LocalDate.now(clock);
        String currentWeek = toIsoWeek(today);
        String previousWeek = toIsoWeek(today.minusWeeks(1));
        LocalDateTime dueAt = resolveDueAt(section, today);

        if (!isActiveWeek(section, currentWeek)) {
            return unavailableWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                team.getTeamId(),
                team.getTeamName(),
                previousWeek,
                formatWeekLabel(previousWeek),
                formatWeekRangeLabel(previousWeek),
                "Peer evaluations can only be submitted during active weeks."
            );
        }

        if (!weekExistsInTimeline(section, previousWeek) || !isActiveWeek(section, previousWeek)) {
            return unavailableWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                team.getTeamId(),
                team.getTeamName(),
                previousWeek,
                formatWeekLabel(previousWeek),
                formatWeekRangeLabel(previousWeek),
                "The previous week is not an active week for peer evaluations."
            );
        }

        Rubric rubric = resolveRubric(section)
            .orElse(null);
        if (rubric == null) {
            return unavailableWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                team.getTeamId(),
                team.getTeamName(),
                previousWeek,
                formatWeekLabel(previousWeek),
                formatWeekRangeLabel(previousWeek),
                "This section does not have a peer evaluation rubric configured."
            );
        }

        List<Criterion> criteria = sortCriteria(rubric.getCriteria());
        validateRubricCriteria(criteria);

        List<User> members = sortMembers(team, student.getId());
        Optional<EvaluationSubmission> existingSubmission = evaluationSubmissionRepository
            .findByEvaluatorStudentIdAndWeek(student.getId(), previousWeek);

        boolean beforeOrAtDeadline = !LocalDateTime.now(clock).isAfter(dueAt);
        if (!beforeOrAtDeadline && existingSubmission.isEmpty()) {
            return new EvaluationWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                team.getTeamId(),
                team.getTeamName(),
                previousWeek,
                formatWeekLabel(previousWeek),
                formatWeekRangeLabel(previousWeek),
                null,
                false,
                false,
                false,
                "The peer-evaluation submission window has closed.",
                dueAt.toString(),
                criteria.stream().map(this::toCriterionDto).toList(),
                List.of()
            );
        }

        EvaluationSubmission submission = existingSubmission.orElse(null);
        return new EvaluationWorkspace(
            section.getSectionId(),
            section.getSectionName(),
            team.getTeamId(),
            team.getTeamName(),
            previousWeek,
            formatWeekLabel(previousWeek),
            formatWeekRangeLabel(previousWeek),
            submission == null ? null : submission.getSubmissionId(),
            submission != null,
            beforeOrAtDeadline,
            beforeOrAtDeadline,
            beforeOrAtDeadline ? null : "The peer-evaluation submission window has closed.",
            dueAt.toString(),
            criteria.stream().map(this::toCriterionDto).toList(),
            buildDraftMembers(members, criteria, submission, student.getId())
        );
    }

    @Transactional
    public PeerEvaluationSubmissionDetail createSubmission(String studentEmail, PeerEvaluationSubmissionRequest request) {
        SubmissionContext context = requireSubmissionContext(studentEmail);
        if (evaluationSubmissionRepository.findByEvaluatorStudentIdAndWeek(context.student().getId(), context.previousWeek()).isPresent()) {
            throw new InvalidPeerEvaluationException("Peer evaluation has already been submitted for this week.");
        }

        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setTeam(context.team());
        submission.setEvaluatorStudent(context.student());
        submission.setWeek(context.previousWeek());

        applySubmission(submission, request, context);
        EvaluationSubmission saved = evaluationSubmissionRepository.save(submission);
        return toSubmissionDetail(saved);
    }

    @Transactional
    public PeerEvaluationSubmissionDetail updateSubmission(String studentEmail, Long submissionId, PeerEvaluationSubmissionRequest request) {
        SubmissionContext context = requireSubmissionContext(studentEmail);
        EvaluationSubmission submission = evaluationSubmissionRepository
            .findBySubmissionIdAndEvaluatorStudentId(submissionId, context.student().getId())
            .orElseThrow(() -> new PeerEvaluationSubmissionNotFoundException(submissionId));

        removeExistingEntries(submission);
        applySubmission(submission, request, context);
        EvaluationSubmission saved = evaluationSubmissionRepository.save(submission);
        return toSubmissionDetail(saved);
    }

    private void applySubmission(
        EvaluationSubmission submission,
        PeerEvaluationSubmissionRequest request,
        SubmissionContext context
    ) {
        Map<Long, User> membersById = context.members().stream()
            .collect(java.util.stream.Collectors.toMap(User::getId, member -> member, (left, right) -> left, LinkedHashMap::new));
        Map<Long, Criterion> criteriaById = context.criteria().stream()
            .collect(java.util.stream.Collectors.toMap(Criterion::getCriterionId, criterion -> criterion, (left, right) -> left, LinkedHashMap::new));

        validateRequest(request, membersById.keySet(), criteriaById);
        for (PeerEvaluationMemberSubmission evaluation : request.evaluations()) {
            EvaluationEntry entry = new EvaluationEntry();
            entry.setEvaluateeStudent(membersById.get(evaluation.evaluateeStudentId()));
            entry.setPublicComment(normalizeComment(evaluation.publicComment(), "Public comments"));
            entry.setPrivateComment(normalizeComment(evaluation.privateComment(), "Private comments"));

            for (PeerEvaluationCriterionScoreSubmission criterionScore : evaluation.criterionScores()) {
                EvaluationScore score = new EvaluationScore();
                score.setCriterion(criteriaById.get(criterionScore.criterionId()));
                score.setScore(requireScore(criterionScore.score(), criteriaById.get(criterionScore.criterionId())));
                entry.addScore(score);
            }

            submission.addEntry(entry);
        }
    }

    private void removeExistingEntries(EvaluationSubmission submission) {
        if (submission.getEntries().isEmpty()) {
            return;
        }

        submission.clearEntries();
        evaluationSubmissionRepository.saveAndFlush(submission);
    }

    private SubmissionContext requireSubmissionContext(String studentEmail) {
        User student = requireStudent(studentEmail);
        Section section = student.getSection();
        if (section == null) {
            throw new InvalidPeerEvaluationException("You must belong to a senior design section before submitting peer evaluations.");
        }

        Team baseTeam = teamRepository.findByStudentIdWithSection(student.getId())
            .orElseThrow(() -> new InvalidPeerEvaluationException("You must be assigned to a senior design team before submitting peer evaluations."));
        Team team = teamRepository.findDetailById(baseTeam.getTeamId())
            .orElse(baseTeam);

        LocalDate today = LocalDate.now(clock);
        String currentWeek = toIsoWeek(today);
        String previousWeek = toIsoWeek(today.minusWeeks(1));
        LocalDateTime dueAt = resolveDueAt(section, today);

        if (!isActiveWeek(section, currentWeek)) {
            throw new InvalidPeerEvaluationException("Peer evaluations can only be submitted during active weeks.");
        }
        if (!weekExistsInTimeline(section, previousWeek) || !isActiveWeek(section, previousWeek)) {
            throw new InvalidPeerEvaluationException("The previous week is not an active week for peer evaluations.");
        }
        if (LocalDateTime.now(clock).isAfter(dueAt)) {
            throw new InvalidPeerEvaluationException("The peer-evaluation submission window has closed.");
        }

        Rubric rubric = resolveRubric(section)
            .orElseThrow(() -> new InvalidPeerEvaluationException("This section does not have a peer evaluation rubric configured."));
        List<Criterion> criteria = sortCriteria(rubric.getCriteria());
        validateRubricCriteria(criteria);

        List<User> members = sortMembers(team, student.getId());
        return new SubmissionContext(student, section, team, previousWeek, dueAt, members, criteria);
    }

    private void validateRequest(
        PeerEvaluationSubmissionRequest request,
        Set<Long> expectedMemberIds,
        Map<Long, Criterion> criteriaById
    ) {
        if (request == null || request.evaluations() == null) {
            throw new InvalidPeerEvaluationException("Peer evaluation request is required.");
        }
        if (request.evaluations().size() != expectedMemberIds.size()) {
            throw new InvalidPeerEvaluationException("Every team member must be evaluated exactly once.");
        }

        Set<Long> seenEvaluatees = new LinkedHashSet<>();
        for (PeerEvaluationMemberSubmission evaluation : request.evaluations()) {
            if (evaluation == null || evaluation.evaluateeStudentId() == null) {
                throw new InvalidPeerEvaluationException("Every evaluation entry must identify the teammate being evaluated.");
            }
            if (!seenEvaluatees.add(evaluation.evaluateeStudentId())) {
                throw new InvalidPeerEvaluationException("Each team member can only appear once in a peer evaluation submission.");
            }
            if (!expectedMemberIds.contains(evaluation.evaluateeStudentId())) {
                throw new InvalidPeerEvaluationException("Peer evaluations may only be submitted for current team members.");
            }
            validateCriterionScores(evaluation.criterionScores(), criteriaById);
            normalizeComment(evaluation.publicComment(), "Public comments");
            normalizeComment(evaluation.privateComment(), "Private comments");
        }

        if (!seenEvaluatees.equals(expectedMemberIds)) {
            throw new InvalidPeerEvaluationException("Every team member must be evaluated exactly once.");
        }
    }

    private void validateCriterionScores(
        List<PeerEvaluationCriterionScoreSubmission> criterionScores,
        Map<Long, Criterion> criteriaById
    ) {
        if (criterionScores == null || criterionScores.size() != criteriaById.size()) {
            throw new InvalidPeerEvaluationException("Every rubric criterion must be scored exactly once.");
        }

        Set<Long> seenCriterionIds = new LinkedHashSet<>();
        for (PeerEvaluationCriterionScoreSubmission criterionScore : criterionScores) {
            if (criterionScore == null || criterionScore.criterionId() == null) {
                throw new InvalidPeerEvaluationException("Every rubric criterion must be scored exactly once.");
            }
            if (!seenCriterionIds.add(criterionScore.criterionId())) {
                throw new InvalidPeerEvaluationException("Each rubric criterion can only be scored once per teammate.");
            }
            Criterion criterion = criteriaById.get(criterionScore.criterionId());
            if (criterion == null) {
                throw new InvalidPeerEvaluationException("Peer evaluations must use the current section rubric.");
            }
            requireScore(criterionScore.score(), criterion);
        }

        if (!seenCriterionIds.equals(criteriaById.keySet())) {
            throw new InvalidPeerEvaluationException("Every rubric criterion must be scored exactly once.");
        }
    }

    private Integer requireScore(BigDecimal score, Criterion criterion) {
        if (score == null) {
            throw new InvalidPeerEvaluationException("Scores are required for every rubric criterion.");
        }
        if (score.stripTrailingZeros().scale() > 0) {
            throw new InvalidPeerEvaluationException("Scores must be integers.");
        }

        int maxScore = requireIntegerMaxScore(criterion);
        int intScore = score.intValueExact();
        if (intScore < 1 || intScore > maxScore) {
            throw new InvalidPeerEvaluationException(
                "Scores must be between 1 and " + maxScore + " for criterion \"" + criterion.getCriterion() + "\"."
            );
        }
        return intScore;
    }

    private String normalizeComment(String value, String label) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > MAX_COMMENT_LENGTH) {
            throw new InvalidPeerEvaluationException(label + " must be 2000 characters or fewer.");
        }
        return normalized;
    }

    private Optional<Rubric> resolveRubric(Section section) {
        if (section.getRubricId() == null) {
            return Optional.empty();
        }
        return rubricRepository.findByIdWithCriteria(section.getRubricId());
    }

    private void validateRubricCriteria(List<Criterion> criteria) {
        if (criteria.isEmpty()) {
            throw new InvalidPeerEvaluationException("This section does not have a peer evaluation rubric configured.");
        }
        for (Criterion criterion : criteria) {
            requireIntegerMaxScore(criterion);
        }
    }

    private int requireIntegerMaxScore(Criterion criterion) {
        double maxScore = criterion.getMaxScore();
        if (maxScore < 1 || maxScore != Math.floor(maxScore)) {
            throw new InvalidPeerEvaluationException("Peer evaluation rubric criteria must use integer max scores.");
        }
        return (int) maxScore;
    }

    private List<User> sortMembers(Team team, Long currentStudentId) {
        List<User> members = new ArrayList<>(team.getStudents());
        members.sort(Comparator
            .comparing((User member) -> !member.getId().equals(currentStudentId))
            .thenComparing(member -> member.getLastName().toLowerCase(Locale.ROOT))
            .thenComparing(member -> member.getFirstName().toLowerCase(Locale.ROOT))
        );

        boolean includesCurrentStudent = members.stream().anyMatch(member -> member.getId().equals(currentStudentId));
        if (!includesCurrentStudent) {
            throw new InvalidPeerEvaluationException("You must be assigned to the current team roster to submit peer evaluations.");
        }
        return members;
    }

    private List<EvaluationMemberDraftDto> buildDraftMembers(
        List<User> members,
        List<Criterion> criteria,
        EvaluationSubmission submission,
        Long currentStudentId
    ) {
        Map<Long, EvaluationEntry> entriesByEvaluateeId = new LinkedHashMap<>();
        if (submission != null) {
            for (EvaluationEntry entry : submission.getEntries()) {
                entriesByEvaluateeId.put(entry.getEvaluateeStudent().getId(), entry);
            }
        }

        return members.stream()
            .map(member -> {
                EvaluationEntry existingEntry = entriesByEvaluateeId.get(member.getId());
                Map<Long, Integer> existingScores = new LinkedHashMap<>();
                if (existingEntry != null) {
                    for (EvaluationScore score : existingEntry.getScores()) {
                        existingScores.put(score.getCriterion().getCriterionId(), score.getScore());
                    }
                }

                List<EvaluationCriterionScoreDraftDto> draftScores = criteria.stream()
                    .map(criterion -> new EvaluationCriterionScoreDraftDto(
                        criterion.getCriterionId(),
                        existingScores.get(criterion.getCriterionId())
                    ))
                    .toList();

                return new EvaluationMemberDraftDto(
                    member.getId(),
                    fullName(member),
                    member.getId().equals(currentStudentId),
                    existingEntry == null ? null : existingEntry.getPublicComment(),
                    existingEntry == null ? null : existingEntry.getPrivateComment(),
                    draftScores
                );
            })
            .toList();
    }

    private EvaluationCriterionDto toCriterionDto(Criterion criterion) {
        return new EvaluationCriterionDto(
            criterion.getCriterionId(),
            criterion.getCriterion(),
            criterion.getDescription(),
            criterion.getMaxScore()
        );
    }

    private PeerEvaluationSubmissionDetail toSubmissionDetail(EvaluationSubmission submission) {
        return new PeerEvaluationSubmissionDetail(
            submission.getSubmissionId(),
            submission.getWeek(),
            submission.getCreatedAt(),
            submission.getUpdatedAt()
        );
    }

    private EvaluationWorkspace unavailableWorkspace(
        Long sectionId,
        String sectionName,
        Long teamId,
        String teamName,
        String week,
        String weekLabel,
        String weekRangeLabel,
        String message
    ) {
        return new EvaluationWorkspace(
            sectionId,
            sectionName,
            teamId,
            teamName,
            week,
            weekLabel,
            weekRangeLabel,
            null,
            false,
            false,
            false,
            message,
            null,
            List.of(),
            List.of()
        );
    }

    private User requireStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new InvalidPeerEvaluationException("No student account found for the current user."));

        if (!isStudent(student)) {
            throw new InvalidPeerEvaluationException("Only students can submit peer evaluations.");
        }
        return student;
    }

    private boolean isStudent(User user) {
        String roles = user.getRoles() == null ? "" : user.getRoles().toLowerCase(Locale.ROOT);
        return List.of(roles.split("\\s+")).contains("student");
    }

    private boolean weekExistsInTimeline(Section section, String week) {
        return deriveSectionWeeks(section).contains(week);
    }

    private boolean isActiveWeek(Section section, String week) {
        return section.getActiveWeeks() != null && section.getActiveWeeks().contains(week);
    }

    private List<String> deriveSectionWeeks(Section section) {
        if (section.getStartDate() == null || section.getEndDate() == null) {
            return section.getActiveWeeks() == null ? List.of() : new ArrayList<>(section.getActiveWeeks());
        }

        LocalDate cursor = toMonday(section.getStartDate());
        LocalDate end = toMonday(section.getEndDate());
        List<String> weeks = new ArrayList<>();
        while (!cursor.isAfter(end)) {
            weeks.add(toIsoWeek(cursor));
            cursor = cursor.plusWeeks(1);
        }
        return weeks;
    }

    private LocalDateTime resolveDueAt(Section section, LocalDate today) {
        DayOfWeek dueDay;
        try {
            dueDay = DayOfWeek.valueOf(section.getPeerEvaluationWeeklyDueDay().trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new InvalidPeerEvaluationException("Peer evaluation due day is not configured correctly for this section.");
        }

        LocalTime dueTime;
        try {
            dueTime = LocalTime.parse(section.getPeerEvaluationDueTime().trim());
        } catch (RuntimeException ex) {
            throw new InvalidPeerEvaluationException("Peer evaluation due time is not configured correctly for this section.");
        }

        LocalDate weekStart = toMonday(today);
        return LocalDateTime.of(weekStart.with(dueDay), dueTime);
    }

    private List<Criterion> sortCriteria(List<Criterion> criteria) {
        return criteria.stream()
            .sorted(Comparator.comparing(Criterion::getCriterionId))
            .toList();
    }

    private String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private LocalDate toMonday(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    private String toIsoWeek(LocalDate date) {
        int weekYear = date.get(ISO.weekBasedYear());
        int weekNumber = date.get(ISO.weekOfWeekBasedYear());
        return "%d-W%02d".formatted(weekYear, weekNumber);
    }

    private String formatWeekLabel(String isoWeek) {
        String[] parts = isoWeek.split("-W");
        return "Week " + Integer.parseInt(parts[1]) + " (" + parts[0] + ")";
    }

    private String formatWeekRangeLabel(String isoWeek) {
        LocalDate start = parseIsoWeekStart(isoWeek);
        LocalDate end = start.plusDays(6);
        return DATE_FORMAT.format(start) + " -- " + DATE_FORMAT.format(end);
    }

    private LocalDate parseIsoWeekStart(String isoWeek) {
        if (isoWeek == null || !isoWeek.matches("\\d{4}-W\\d{2}")) {
            throw new InvalidPeerEvaluationException("Invalid ISO week format.");
        }
        String[] parts = isoWeek.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        return LocalDate.of(year, 1, 4)
            .with(ISO.weekOfWeekBasedYear(), week)
            .with(ISO.dayOfWeek(), 1);
    }

    private record SubmissionContext(
        User student,
        Section section,
        Team team,
        String previousWeek,
        LocalDateTime dueAt,
        List<User> members,
        List<Criterion> criteria
    ) {
    }
}

package team.projectpulse.evaluation.service;

import org.junit.jupiter.api.Test;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.domain.EvaluationSubmission;
import team.projectpulse.evaluation.domain.InvalidPeerEvaluationException;
import team.projectpulse.evaluation.domain.PeerEvaluationSubmissionNotFoundException;
import team.projectpulse.evaluation.dto.EvaluationWorkspace;
import team.projectpulse.evaluation.dto.PeerEvaluationCriterionScoreSubmission;
import team.projectpulse.evaluation.dto.PeerEvaluationMemberSubmission;
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationServiceTest {

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-04-22T15:00:00Z"),
        ZoneId.of("America/Chicago")
    );

    @Test
    void should_LoadUnavailableWorkspace_When_StudentHasNoSection() {
        User student = buildStudent(100L, "Ava", "Johnson", null);
        EvaluationService service = buildService(student, null, null, null, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertFalse(workspace.canSubmit());
        assertEquals("You must belong to a senior design section before submitting peer evaluations.", workspace.availabilityMessage());
    }

    @Test
    void should_LoadUnavailableWorkspace_When_StudentHasNoTeam() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        EvaluationService service = buildService(student, null, null, null, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertFalse(workspace.canSubmit());
        assertEquals("You must be assigned to a senior design team before submitting peer evaluations.", workspace.availabilityMessage());
    }

    @Test
    void should_LoadUnavailableWorkspace_When_CurrentWeekIsInactive() {
        Section section = buildSection(List.of(previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student, buildStudent(101L, "Mia", "Chen", section));
        EvaluationService service = buildService(student, team, buildRubric(10.0), null, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertFalse(workspace.canSubmit());
        assertEquals("Peer evaluations can only be submitted during active weeks.", workspace.availabilityMessage());
    }

    @Test
    void should_LoadUnavailableWorkspace_When_PreviousWeekIsNotActive() {
        Section section = buildSection(List.of(currentIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student, buildStudent(101L, "Mia", "Chen", section));
        EvaluationService service = buildService(student, team, buildRubric(10.0), null, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertFalse(workspace.canSubmit());
        assertEquals("The previous week is not an active week for peer evaluations.", workspace.availabilityMessage());
    }

    @Test
    void should_LoadEditableWorkspaceWithCurrentTeamAndRubric() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertTrue(workspace.canSubmit());
        assertTrue(workspace.editable());
        assertFalse(workspace.submitted());
        assertEquals(previousIsoWeek(clock), workspace.week());
        assertEquals(2, workspace.criteria().size());
        assertEquals(2, workspace.members().size());
        assertTrue(workspace.members().get(0).self());
        assertEquals("Ava Johnson", workspace.members().get(0).fullName());
    }

    @Test
    void should_LoadExistingSubmissionIntoWorkspace() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationSubmission submission = buildSubmission(team, student, teammate, rubric.getCriteria(), previousIsoWeek(clock));
        EvaluationService service = buildService(student, team, rubric, submission, clock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertTrue(workspace.submitted());
        assertEquals(900L, workspace.submissionId());
        assertEquals("Great teammate", workspace.members().get(1).publicComment());
        assertEquals(8, workspace.members().get(1).scores().get(0).score());
    }

    @Test
    void should_ReturnReadOnlyWorkspace_When_DeadlinePassedAndSubmissionExists() {
        Clock lateClock = Clock.fixed(Instant.parse("2026-04-25T06:00:00Z"), ZoneId.of("America/Chicago"));
        Section section = buildSection(List.of(currentIsoWeek(lateClock), previousIsoWeek(lateClock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationSubmission submission = buildSubmission(team, student, teammate, rubric.getCriteria(), previousIsoWeek(lateClock));
        EvaluationService service = buildService(student, team, rubric, submission, lateClock);

        EvaluationWorkspace workspace = service.loadWorkspace("ava@tcu.edu");

        assertFalse(workspace.canSubmit());
        assertFalse(workspace.editable());
        assertTrue(workspace.submitted());
        assertEquals("The peer-evaluation submission window has closed.", workspace.availabilityMessage());
    }

    @Test
    void should_CreateSubmission_When_RequestIsValid() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        var detail = service.createSubmission("ava@tcu.edu", validRequest(student, teammate, rubric.getCriteria()));

        assertEquals(901L, detail.submissionId());
        assertEquals(previousIsoWeek(clock), detail.week());
        assertNotNull(detail.submittedAt());
    }

    @Test
    void should_UpdateSubmission_When_RequestIsValidAndBeforeDeadline() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationSubmission submission = buildSubmission(team, student, teammate, rubric.getCriteria(), previousIsoWeek(clock));
        EvaluationService service = buildService(student, team, rubric, submission, clock);

        var detail = service.updateSubmission("ava@tcu.edu", 900L, validRequest(student, teammate, rubric.getCriteria()));

        assertEquals(900L, detail.submissionId());
    }

    @Test
    void should_RejectSubmissionAfterDeadline() {
        Clock lateClock = Clock.fixed(Instant.parse("2026-04-25T06:00:00Z"), ZoneId.of("America/Chicago"));
        Section section = buildSection(List.of(currentIsoWeek(lateClock), previousIsoWeek(lateClock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, lateClock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission("ava@tcu.edu", validRequest(student, teammate, rubric.getCriteria()))
        );

        assertEquals("The peer-evaluation submission window has closed.", ex.getMessage());
    }

    @Test
    void should_RejectDuplicateEvaluatees() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(student.getId(), null, null, validScores(rubric.getCriteria())),
                    new PeerEvaluationMemberSubmission(student.getId(), null, null, validScores(rubric.getCriteria()))
                ))
            )
        );

        assertEquals("Each team member can only appear once in a peer evaluation submission.", ex.getMessage());
    }

    @Test
    void should_RejectMissingEvaluatee() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(student.getId(), null, null, validScores(rubric.getCriteria()))
                ))
            )
        );

        assertEquals("Every team member must be evaluated exactly once.", ex.getMessage());
    }

    @Test
    void should_RejectEvaluateeOutsideCurrentTeam() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        User teammate = buildStudent(101L, "Mia", "Chen", section);
        User outsider = buildStudent(999L, "Noah", "Bennett", section);
        Team team = buildTeam(section, student, teammate);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(student.getId(), null, null, validScores(rubric.getCriteria())),
                    new PeerEvaluationMemberSubmission(outsider.getId(), null, null, validScores(rubric.getCriteria()))
                ))
            )
        );

        assertEquals("Peer evaluations may only be submitted for current team members.", ex.getMessage());
    }

    @Test
    void should_RejectMissingCriterionScore() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(
                        student.getId(),
                        null,
                        null,
                        List.of(new PeerEvaluationCriterionScoreSubmission(501L, BigDecimal.TEN))
                    )
                ))
            )
        );

        assertEquals("Every rubric criterion must be scored exactly once.", ex.getMessage());
    }

    @Test
    void should_RejectDuplicateCriterionScore() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(
                        student.getId(),
                        null,
                        null,
                        List.of(
                            new PeerEvaluationCriterionScoreSubmission(501L, BigDecimal.TEN),
                            new PeerEvaluationCriterionScoreSubmission(501L, BigDecimal.valueOf(9))
                        )
                    )
                ))
            )
        );

        assertEquals("Each rubric criterion can only be scored once per teammate.", ex.getMessage());
    }

    @Test
    void should_RejectOutOfRangeScore() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.createSubmission(
                "ava@tcu.edu",
                new PeerEvaluationSubmissionRequest(List.of(
                    new PeerEvaluationMemberSubmission(
                        student.getId(),
                        null,
                        null,
                        List.of(
                            new PeerEvaluationCriterionScoreSubmission(501L, BigDecimal.valueOf(11)),
                            new PeerEvaluationCriterionScoreSubmission(502L, BigDecimal.TEN)
                        )
                    )
                ))
            )
        );

        assertEquals("Scores must be between 1 and 10 for criterion \"Participation\".", ex.getMessage());
    }

    @Test
    void should_RejectNonIntegerRubricConfiguration() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student);
        Rubric rubric = buildRubric(9.5);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        InvalidPeerEvaluationException ex = assertThrows(
            InvalidPeerEvaluationException.class,
            () -> service.loadWorkspace("ava@tcu.edu")
        );

        assertEquals("Peer evaluation rubric criteria must use integer max scores.", ex.getMessage());
    }

    @Test
    void should_ReturnNotFound_When_UpdatingMissingOwnedSubmission() {
        Section section = buildSection(List.of(currentIsoWeek(clock), previousIsoWeek(clock)));
        User student = buildStudent(100L, "Ava", "Johnson", section);
        Team team = buildTeam(section, student);
        Rubric rubric = buildRubric(10.0);
        EvaluationService service = buildService(student, team, rubric, null, clock);

        assertThrows(
            PeerEvaluationSubmissionNotFoundException.class,
            () -> service.updateSubmission("ava@tcu.edu", 999L, validRequest(student, null, rubric.getCriteria()))
        );
    }

    private EvaluationService buildService(
        User student,
        Team team,
        Rubric rubric,
        EvaluationSubmission submission,
        Clock activeClock
    ) {
        AtomicLong submissionIds = new AtomicLong(901L);
        UserRepository userRepository = proxy(UserRepository.class, (method, args) -> switch (method.getName()) {
            case "findByEmail" -> Optional.ofNullable(student);
            default -> defaultValue(method);
        });
        TeamRepository teamRepository = proxy(TeamRepository.class, (method, args) -> switch (method.getName()) {
            case "findByStudentIdWithSection" -> Optional.ofNullable(team);
            case "findDetailById" -> Optional.ofNullable(team);
            default -> defaultValue(method);
        });
        RubricRepository rubricRepository = proxy(RubricRepository.class, (method, args) -> switch (method.getName()) {
            case "findByIdWithCriteria" -> Optional.ofNullable(rubric);
            default -> defaultValue(method);
        });
        EvaluationSubmissionRepository submissionRepository = proxy(EvaluationSubmissionRepository.class, (method, args) -> switch (method.getName()) {
            case "findByEvaluatorStudentIdAndWeek" -> submission != null && submission.getEvaluatorStudent().getId().equals(args[0]) && submission.getWeek().equals(args[1])
                ? Optional.of(submission)
                : Optional.empty();
            case "findBySubmissionIdAndEvaluatorStudentId" -> submission != null
                && submission.getSubmissionId().equals(args[0])
                && submission.getEvaluatorStudent().getId().equals(args[1])
                ? Optional.of(submission)
                : Optional.empty();
            case "save" -> {
                EvaluationSubmission saved = (EvaluationSubmission) args[0];
                if (saved.getSubmissionId() == null) {
                    saved.setSubmissionId(submissionIds.getAndIncrement());
                }
                if (saved.getCreatedAt() == null) {
                    saved.setCreatedAt(java.time.LocalDateTime.now(activeClock));
                }
                saved.setUpdatedAt(java.time.LocalDateTime.now(activeClock));
                yield saved;
            }
            default -> defaultValue(method);
        });

        return new EvaluationService(submissionRepository, userRepository, teamRepository, rubricRepository, activeClock);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, RepositoryCall handler) {
        InvocationHandler invocationHandler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> type.getSimpleName() + "Proxy";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                };
            }
            return handler.invoke(method, args == null ? new Object[0] : args);
        };
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, invocationHandler);
    }

    private Object defaultValue(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType.equals(boolean.class)) {
            return false;
        }
        if (returnType.equals(int.class) || returnType.equals(long.class) || returnType.equals(short.class) || returnType.equals(byte.class)) {
            return 0;
        }
        if (returnType.equals(double.class) || returnType.equals(float.class)) {
            return 0.0;
        }
        return null;
    }

    private Section buildSection(List<String> activeWeeks) {
        Section section = new Section();
        section.setSectionId(1L);
        section.setSectionName("Spring 2026 - Section A");
        section.setStartDate(LocalDate.of(2026, 1, 12));
        section.setEndDate(LocalDate.of(2026, 5, 8));
        section.setRubricId(11L);
        section.setActiveWeeks(activeWeeks);
        section.setPeerEvaluationWeeklyDueDay("FRIDAY");
        section.setPeerEvaluationDueTime("23:59");
        return section;
    }

    private Team buildTeam(Section section, User... students) {
        Team team = new Team();
        team.setTeamId(2001L);
        team.setTeamName("Pulse Analytics");
        team.setSection(section);
        team.setStudents(new LinkedHashSet<>(List.of(students)));
        return team;
    }

    private User buildStudent(Long id, String firstName, String lastName, Section section) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase(Locale.ROOT) + "@tcu.edu");
        user.setPassword("encoded");
        user.setRoles("student");
        user.setEnabled(true);
        user.setSection(section);
        return user;
    }

    private Rubric buildRubric(double maxScore) {
        Criterion participation = new Criterion();
        participation.setCriterionId(501L);
        participation.setCriterion("Participation");
        participation.setDescription("Participates");
        participation.setMaxScore(maxScore);

        Criterion communication = new Criterion();
        communication.setCriterionId(502L);
        communication.setCriterion("Communication");
        communication.setDescription("Communicates");
        communication.setMaxScore(10.0);

        Rubric rubric = new Rubric();
        rubric.setRubricId(11L);
        rubric.setRubricName("Default Peer Evaluation Rubric");
        rubric.setCriteria(List.of(participation, communication));
        return rubric;
    }

    private EvaluationSubmission buildSubmission(Team team, User evaluator, User teammate, List<Criterion> criteria, String week) {
        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setSubmissionId(900L);
        submission.setTeam(team);
        submission.setEvaluatorStudent(evaluator);
        submission.setWeek(week);
        submission.setCreatedAt(java.time.LocalDateTime.now(clock));
        submission.setUpdatedAt(java.time.LocalDateTime.now(clock));

        EvaluationEntry selfEntry = new EvaluationEntry();
        selfEntry.setEntryId(700L);
        selfEntry.setEvaluateeStudent(evaluator);
        selfEntry.setPublicComment("Self review");
        selfEntry.setPrivateComment("Private self review");
        selfEntry.addScore(buildScore(criteria.get(0), 9));
        selfEntry.addScore(buildScore(criteria.get(1), 10));

        EvaluationEntry teammateEntry = new EvaluationEntry();
        teammateEntry.setEntryId(701L);
        teammateEntry.setEvaluateeStudent(teammate);
        teammateEntry.setPublicComment("Great teammate");
        teammateEntry.setPrivateComment("Very reliable");
        teammateEntry.addScore(buildScore(criteria.get(0), 8));
        teammateEntry.addScore(buildScore(criteria.get(1), 9));

        submission.addEntry(selfEntry);
        submission.addEntry(teammateEntry);
        return submission;
    }

    private EvaluationScore buildScore(Criterion criterion, int value) {
        EvaluationScore score = new EvaluationScore();
        score.setCriterion(criterion);
        score.setScore(value);
        return score;
    }

    private PeerEvaluationSubmissionRequest validRequest(User evaluator, User teammate, List<Criterion> criteria) {
        List<PeerEvaluationMemberSubmission> evaluations = new java.util.ArrayList<>();
        evaluations.add(new PeerEvaluationMemberSubmission(
            evaluator.getId(),
            "Self comment",
            "Self private",
            validScores(criteria)
        ));
        if (teammate != null) {
            evaluations.add(new PeerEvaluationMemberSubmission(
                teammate.getId(),
                "Teammate comment",
                "Teammate private",
                validScores(criteria)
            ));
        }
        return new PeerEvaluationSubmissionRequest(evaluations);
    }

    private List<PeerEvaluationCriterionScoreSubmission> validScores(List<Criterion> criteria) {
        return criteria.stream()
            .map(criterion -> new PeerEvaluationCriterionScoreSubmission(
                criterion.getCriterionId(),
                BigDecimal.valueOf(Math.min(10, (int) criterion.getMaxScore()))
            ))
            .toList();
    }

    private static String currentIsoWeek(Clock activeClock) {
        LocalDate now = LocalDate.now(activeClock);
        return "%d-W%02d".formatted(now.get(WeekFields.ISO.weekBasedYear()), now.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    private static String previousIsoWeek(Clock activeClock) {
        LocalDate previous = LocalDate.now(activeClock).minusWeeks(1);
        return "%d-W%02d".formatted(previous.get(WeekFields.ISO.weekBasedYear()), previous.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    @FunctionalInterface
    private interface RepositoryCall {
        Object invoke(Method method, Object[] args) throws Throwable;
    }
}

package team.projectpulse.report.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.domain.EvaluationSubmission;
import team.projectpulse.evaluation.repository.EvaluationSubmissionRepository;
import team.projectpulse.report.domain.InvalidPeerEvaluationReportException;
import team.projectpulse.report.dto.StudentPeerEvaluationReportResponse;
import team.projectpulse.rubric.domain.Criterion;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private EvaluationSubmissionRepository evaluationSubmissionRepository;

    @Mock
    private UserRepository userRepository;

    private final Clock clock = Clock.fixed(
        Instant.parse("2026-04-24T12:00:00Z"),
        ZoneId.of("America/Chicago")
    );

    @Test
    void should_LoadDefaultPreviousActiveWeek_When_WeekIsOmitted() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W14", "2026-W15", "2026-W16"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));
        when(evaluationSubmissionRepository.findEntriesByEvaluateeStudentIdAndWeek(1003L, "2026-W16")).thenReturn(List.of(teammateEntry(student, section)));

        StudentPeerEvaluationReportResponse response = service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null);

        assertEquals("2026-W16", response.selectedWeek());
        assertTrue(response.reportAvailable());
    }

    @Test
    void should_FallBackToMostRecentPastWeek_When_PreviousWeekIsNotSelectable() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W13", "2026-W15"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));
        when(evaluationSubmissionRepository.findEntriesByEvaluateeStudentIdAndWeek(1003L, "2026-W15")).thenReturn(List.of(teammateEntry(student, section)));

        StudentPeerEvaluationReportResponse response = service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null);

        assertEquals("2026-W15", response.selectedWeek());
    }

    @Test
    void should_RejectRequestedWeek_When_NotActive() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W15"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));

        InvalidPeerEvaluationReportException ex = assertThrows(
            InvalidPeerEvaluationReportException.class,
            () -> service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", "2026-W14")
        );

        assertEquals("The selected week is not available for your peer evaluation report.", ex.getMessage());
    }

    @Test
    void should_RejectRequestedWeek_When_CurrentOrFuture() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W16", "2026-W17", "2026-W18"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));

        InvalidPeerEvaluationReportException ex = assertThrows(
            InvalidPeerEvaluationReportException.class,
            () -> service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", "2026-W17")
        );

        assertEquals("The selected week is not available for your peer evaluation report.", ex.getMessage());
    }

    @Test
    void should_Reject_When_UserIsNotStudent() {
        ReportService service = service();
        User instructor = buildUser(3001L, "Ivy", "Stone", "instructor", null);

        when(userRepository.findByEmail("ivy.stone@tcu.edu")).thenReturn(Optional.of(instructor));

        InvalidPeerEvaluationReportException ex = assertThrows(
            InvalidPeerEvaluationReportException.class,
            () -> service.generateOwnPeerEvaluationReport("ivy.stone@tcu.edu", null)
        );

        assertEquals("Only students can view their peer evaluation report.", ex.getMessage());
    }

    @Test
    void should_Reject_When_StudentHasNoSection() {
        ReportService service = service();
        User student = buildStudent(1003L, "Ava", "Johnson", null);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));

        InvalidPeerEvaluationReportException ex = assertThrows(
            InvalidPeerEvaluationReportException.class,
            () -> service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null)
        );

        assertEquals("You must belong to a senior design section to view your peer evaluation report.", ex.getMessage());
    }

    @Test
    void should_ReturnNoDataResponse_When_NoTeammateEvaluationsExist() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W16"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));
        when(evaluationSubmissionRepository.findEntriesByEvaluateeStudentIdAndWeek(1003L, "2026-W16")).thenReturn(List.of(selfEntry(student, section)));

        StudentPeerEvaluationReportResponse response = service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null);

        assertFalse(response.reportAvailable());
        assertEquals("No peer evaluation data is available for the selected week.", response.availabilityMessage());
        assertTrue(response.publicComments().isEmpty());
        assertNull(response.overallGrade());
    }

    @Test
    void should_ExcludeSelfEvaluationAndPrivateComments_When_ComputingReport() {
        ReportService service = service();
        Section section = buildSection(List.of("2026-W16"));
        User student = buildStudent(1003L, "Ava", "Johnson", section);
        EvaluationEntry self = selfEntry(student, section);
        EvaluationEntry teammateOne = teammateEntry(student, section);
        EvaluationEntry teammateTwo = teammateEntry(student, section);
        teammateTwo.getScores().get(0).setScore(6);
        teammateTwo.getScores().get(1).setScore(8);
        teammateTwo.setPublicComment("Needs more consistency.");

        when(userRepository.findByEmail("ava.johnson@tcu.edu")).thenReturn(Optional.of(student));
        when(evaluationSubmissionRepository.findEntriesByEvaluateeStudentIdAndWeek(1003L, "2026-W16"))
            .thenReturn(List.of(self, teammateOne, teammateTwo));

        StudentPeerEvaluationReportResponse response = service.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null);

        assertTrue(response.reportAvailable());
        assertEquals(new BigDecimal("7.50"), response.criterionAverages().get(0).averageScore());
        assertEquals(new BigDecimal("9.00"), response.criterionAverages().get(1).averageScore());
        assertEquals(new BigDecimal("16.50"), response.overallGrade());
        assertEquals(List.of("Great teammate.", "Needs more consistency."), response.publicComments());
        assertEquals(20, response.maxTotalScore());
    }

    private ReportService service() {
        return new ReportService(
            teamRepository,
            activityRepository,
            evaluationSubmissionRepository,
            userRepository,
            clock
        );
    }

    private Section buildSection(List<String> activeWeeks) {
        Section section = new Section();
        section.setSectionId(1L);
        section.setSectionName("Spring 2026 - Section A");
        section.setStartDate(LocalDate.of(2026, 1, 12));
        section.setEndDate(LocalDate.of(2026, 5, 8));
        section.setActiveWeeks(activeWeeks);
        return section;
    }

    private User buildStudent(Long id, String firstName, String lastName, Section section) {
        return buildUser(id, firstName, lastName, "student", section);
    }

    private User buildUser(Long id, String firstName, String lastName, String role, Section section) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@tcu.edu");
        user.setRoles(role);
        user.setEnabled(true);
        user.setSection(section);
        return user;
    }

    private EvaluationEntry selfEntry(User student, Section section) {
        User selfEvaluator = buildStudent(student.getId(), student.getFirstName(), student.getLastName(), section);
        return buildEntry(student, selfEvaluator, "Self reflection", "Instructor only", 10, 10);
    }

    private EvaluationEntry teammateEntry(User student, Section section) {
        User evaluator = buildStudent(1004L, "Liam", "Parker", section);
        return buildEntry(student, evaluator, "Great teammate.", "Private note", 9, 10);
    }

    private EvaluationEntry buildEntry(User evaluatee, User evaluator, String publicComment, String privateComment, int scoreOne, int scoreTwo) {
        Criterion participation = criterion(1L, "Participation", "Participates", 10);
        Criterion communication = criterion(2L, "Communication", "Communicates", 10);

        EvaluationEntry entry = new EvaluationEntry();
        entry.setEntryId(evaluator.getId() + 1000);
        entry.setEvaluateeStudent(evaluatee);
        entry.setPublicComment(publicComment);
        entry.setPrivateComment(privateComment);

        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setSubmissionId(evaluator.getId() + 5000);
        submission.setWeek("2026-W16");
        submission.setEvaluatorStudent(evaluator);
        Team team = new Team();
        team.setTeamId(2001L);
        team.setTeamName("Pulse Analytics");
        team.setSection(evaluatee.getSection());
        submission.setTeam(team);
        entry.setSubmission(submission);

        entry.addScore(score(participation, scoreOne));
        entry.addScore(score(communication, scoreTwo));
        return entry;
    }

    private EvaluationScore score(Criterion criterion, int value) {
        EvaluationScore score = new EvaluationScore();
        score.setCriterion(criterion);
        score.setScore(value);
        return score;
    }

    private Criterion criterion(Long id, String name, String description, double maxScore) {
        Criterion criterion = new Criterion();
        criterion.setCriterionId(id);
        criterion.setCriterion(name);
        criterion.setDescription(description);
        criterion.setMaxScore(maxScore);
        return criterion;
    }
}

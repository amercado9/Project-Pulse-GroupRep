package team.projectpulse.evaluation.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import team.projectpulse.evaluation.domain.EvaluationEntry;
import team.projectpulse.evaluation.domain.EvaluationScore;
import team.projectpulse.evaluation.domain.EvaluationSubmission;
import team.projectpulse.rubric.domain.Criterion;
import team.projectpulse.rubric.domain.Rubric;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.user.domain.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class EvaluationSubmissionRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private EvaluationSubmissionRepository evaluationSubmissionRepository;

    @Test
    void should_FetchSubmissionWithEntriesAndScores_When_QueryingByEvaluatorAndWeek() {
        SeededData data = seedSubmission();

        Optional<EvaluationSubmission> submission = evaluationSubmissionRepository.findByEvaluatorStudentIdAndWeek(
            data.evaluator().getId(),
            "2026-W16"
        );

        assertTrue(submission.isPresent());
        assertEquals(2, submission.get().getEntries().size());
        assertEquals(2, submission.get().getEntries().get(0).getScores().size());
    }

    @Test
    void should_EnforceOneSubmissionPerEvaluatorPerWeek() {
        SeededData data = seedSubmission();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            EvaluationSubmission duplicate = new EvaluationSubmission();
            duplicate.setTeam(data.team());
            duplicate.setEvaluatorStudent(data.evaluator());
            duplicate.setWeek("2026-W16");
            entityManager.persist(duplicate);
            entityManager.flush();
            entityManager.clear();
        });
        assertTrue(hasCause(ex, ConstraintViolationException.class));
    }

    @Test
    void should_DeleteEntriesAndScores_When_SubmissionIsDeleted() {
        SeededData data = seedSubmission();

        evaluationSubmissionRepository.delete(data.submission());
        entityManager.flush();

        Long entryCount = entityManager.createQuery(
            "select count(e) from EvaluationEntry e",
            Long.class
        ).getSingleResult();
        Long scoreCount = entityManager.createQuery(
            "select count(s) from EvaluationScore s",
            Long.class
        ).getSingleResult();

        assertEquals(0L, entryCount);
        assertEquals(0L, scoreCount);
    }

    @Test
    void should_AllowReplacingEntriesWithSameEvaluatees_When_SubmissionIsUpdated() {
        SeededData data = seedSubmission();

        EvaluationSubmission managedSubmission = evaluationSubmissionRepository
            .findBySubmissionIdAndEvaluatorStudentId(data.submission().getSubmissionId(), data.evaluator().getId())
            .orElseThrow();

        managedSubmission.clearEntries();
        evaluationSubmissionRepository.saveAndFlush(managedSubmission);

        EvaluationEntry selfEntry = new EvaluationEntry();
        selfEntry.setEvaluateeStudent(data.evaluator());
        selfEntry.setPublicComment("Updated self");
        selfEntry.setPrivateComment("Updated private self");
        selfEntry.addScore(persistScore(data.participation(), 10));
        selfEntry.addScore(persistScore(data.communication(), 10));

        EvaluationEntry teammateEntry = new EvaluationEntry();
        teammateEntry.setEvaluateeStudent(data.teammate());
        teammateEntry.setPublicComment("Updated teammate");
        teammateEntry.setPrivateComment("Updated private teammate");
        teammateEntry.addScore(persistScore(data.participation(), 9));
        teammateEntry.addScore(persistScore(data.communication(), 9));

        managedSubmission.addEntry(selfEntry);
        managedSubmission.addEntry(teammateEntry);

        assertDoesNotThrow(() -> evaluationSubmissionRepository.saveAndFlush(managedSubmission));
        assertEquals(2, managedSubmission.getEntries().size());
    }

    private SeededData seedSubmission() {
        Criterion participation = new Criterion();
        participation.setCriterion("Participation");
        participation.setDescription("Participates");
        participation.setMaxScore(10);
        entityManager.persist(participation);

        Criterion communication = new Criterion();
        communication.setCriterion("Communication");
        communication.setDescription("Communicates");
        communication.setMaxScore(10);
        entityManager.persist(communication);

        Rubric rubric = new Rubric();
        rubric.setRubricName("Evaluation Test Rubric");
        rubric.setCriteria(List.of(participation, communication));
        entityManager.persist(rubric);

        Section section = new Section();
        section.setSectionName("Spring 2026 - Section A");
        section.setStartDate(LocalDate.of(2026, 1, 12));
        section.setEndDate(LocalDate.of(2026, 5, 8));
        section.setRubricId(rubric.getRubricId());
        entityManager.persist(section);

        User ava = persistUser("Ava", "Johnson", "ava.evaluation@tcu.edu", section);
        User mia = persistUser("Mia", "Chen", "mia.evaluation@tcu.edu", section);

        Team team = new Team();
        team.setSection(section);
        team.setTeamName("Pulse Analytics Evaluation");
        team.getStudents().add(ava);
        team.getStudents().add(mia);
        entityManager.persist(team);

        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setTeam(team);
        submission.setEvaluatorStudent(ava);
        submission.setWeek("2026-W16");

        EvaluationEntry selfEntry = new EvaluationEntry();
        selfEntry.setEvaluateeStudent(ava);
        selfEntry.setPublicComment("Self");
        selfEntry.setPrivateComment("Private self");
        selfEntry.addScore(persistScore(participation, 9));
        selfEntry.addScore(persistScore(communication, 10));

        EvaluationEntry teammateEntry = new EvaluationEntry();
        teammateEntry.setEvaluateeStudent(mia);
        teammateEntry.setPublicComment("Great teammate");
        teammateEntry.setPrivateComment("Reliable");
        teammateEntry.addScore(persistScore(participation, 8));
        teammateEntry.addScore(persistScore(communication, 9));

        submission.addEntry(selfEntry);
        submission.addEntry(teammateEntry);
        entityManager.persist(submission);
        entityManager.flush();
        entityManager.clear();

        return new SeededData(ava, mia, team, submission, participation, communication);
    }

    private EvaluationScore persistScore(Criterion criterion, int value) {
        EvaluationScore score = new EvaluationScore();
        score.setCriterion(criterion);
        score.setScore(value);
        return score;
    }

    private User persistUser(String firstName, String lastName, String email, Section section) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles("student");
        user.setEnabled(true);
        user.setSection(section);
        entityManager.persist(user);
        return user;
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> expectedType) {
        Throwable current = throwable;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private record SeededData(
        User evaluator,
        User teammate,
        Team team,
        EvaluationSubmission submission,
        Criterion participation,
        Criterion communication
    ) {
    }
}

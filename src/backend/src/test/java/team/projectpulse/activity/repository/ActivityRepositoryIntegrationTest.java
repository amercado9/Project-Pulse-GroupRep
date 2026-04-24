package team.projectpulse.activity.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.domain.ActivityCategory;
import team.projectpulse.activity.domain.ActivityStatus;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.user.domain.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class ActivityRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ActivityRepository activityRepository;

    @Test
    void should_ReturnOnlyCurrentStudentsActivitiesForWeek_When_QueryingByStudentAndWeek() {
        SeededData data = seedActivities();

        List<Activity> activities = activityRepository.findAllByStudentIdAndWeekOrderByActivityIdAsc(
            data.ava().getId(),
            "2026-W16"
        );

        assertEquals(List.of("Implement endpoint", "Write tests"), activities.stream()
            .map(Activity::getPlannedActivity)
            .toList());
    }

    @Test
    void should_EnforceOwnership_When_FindingActivityByIdAndStudentId() {
        SeededData data = seedActivities();

        Optional<Activity> ownedActivity = activityRepository.findByActivityIdAndStudentId(
            data.firstAvaActivity().getActivityId(),
            data.ava().getId()
        );
        Optional<Activity> foreignActivity = activityRepository.findByActivityIdAndStudentId(
            data.ethanActivity().getActivityId(),
            data.ava().getId()
        );

        assertTrue(ownedActivity.isPresent());
        assertTrue(foreignActivity.isEmpty());
    }

    private SeededData seedActivities() {
        Section section = new Section();
        section.setSectionName("Spring 2026 - Section A");
        section.setStartDate(LocalDate.of(2026, 1, 12));
        section.setEndDate(LocalDate.of(2026, 5, 8));
        entityManager.persist(section);

        User ava = persistUser("Ava", "Johnson", "ava.activity@tcu.edu");
        User ethan = persistUser("Ethan", "Wright", "ethan.activity@tcu.edu");

        Team team = new Team();
        team.setSection(section);
        team.setTeamName("Pulse Analytics");
        entityManager.persist(team);

        Activity firstAva = persistActivity(team, ava, "2026-W16", "Implement endpoint");
        Activity secondAva = persistActivity(team, ava, "2026-W16", "Write tests");
        Activity ethanActivity = persistActivity(team, ethan, "2026-W16", "Review PR");
        persistActivity(team, ava, "2026-W15", "Older week");

        entityManager.flush();
        entityManager.clear();

        return new SeededData(ava, firstAva, ethanActivity);
    }

    private User persistUser(String firstName, String lastName, String email) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles("student");
        user.setEnabled(true);
        entityManager.persist(user);
        return user;
    }

    private Activity persistActivity(Team team, User student, String week, String plannedActivity) {
        Activity activity = new Activity();
        activity.setTeam(team);
        activity.setStudent(student);
        activity.setWeek(week);
        activity.setCategory(ActivityCategory.DEVELOPMENT);
        activity.setPlannedActivity(plannedActivity);
        activity.setDescription(plannedActivity + " description");
        activity.setPlannedHours(new BigDecimal("2.00"));
        activity.setActualHours(new BigDecimal("3.00"));
        activity.setStatus(ActivityStatus.DONE);
        entityManager.persist(activity);
        return activity;
    }

    private record SeededData(User ava, Activity firstAvaActivity, Activity ethanActivity) {
    }
}

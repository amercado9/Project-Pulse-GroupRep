package team.projectpulse.activity.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.domain.ActivityCategory;
import team.projectpulse.activity.domain.ActivityNotFoundException;
import team.projectpulse.activity.domain.ActivityStatus;
import team.projectpulse.activity.domain.InvalidActivityException;
import team.projectpulse.activity.dto.ActivityWorkspace;
import team.projectpulse.activity.dto.CreateActivityRequest;
import team.projectpulse.activity.dto.UpdateActivityRequest;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private ActivityService service;

    @Test
    void should_LoadWorkspaceForStudent_When_ContextExists() {
        Section section = buildSection(LocalDate.now().minusWeeks(2), LocalDate.now().plusWeeks(2), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);
        Activity activity = buildActivity(1L, team, student, currentIsoWeek());

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.findAllByStudentIdAndWeekOrderByActivityIdAsc(100L, currentIsoWeek()))
            .thenReturn(List.of(activity));

        ActivityWorkspace workspace = service.loadWorkspace("ava@tcu.edu", currentIsoWeek());

        assertTrue(workspace.canManageActivities());
        assertEquals("Spring 2026 - Section A", workspace.sectionName());
        assertEquals("Pulse Analytics", workspace.teamName());
        assertEquals(currentIsoWeek(), workspace.selectedWeek());
        assertEquals(1, workspace.activities().size());
        assertTrue(workspace.availableWeeks().stream().anyMatch(option -> option.activeWeek()));
    }

    @Test
    void should_DefaultToLatestEligibleWeek_When_RequestWeekMissing() {
        Section section = buildSection(LocalDate.now().minusWeeks(3), LocalDate.now().plusWeeks(2), List.of(previousIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.findAllByStudentIdAndWeekOrderByActivityIdAsc(100L, currentIsoWeek()))
            .thenReturn(List.of());

        ActivityWorkspace workspace = service.loadWorkspace("ava@tcu.edu", null);

        assertEquals(currentIsoWeek(), workspace.selectedWeek());
    }

    @Test
    void should_ReturnDisabledWorkspace_When_StudentHasNoSection() {
        User student = buildStudent(100L, null);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));

        ActivityWorkspace workspace = service.loadWorkspace("ava@tcu.edu", null);

        assertFalse(workspace.canManageActivities());
        assertEquals("You must belong to a senior design section before managing WAR activities.", workspace.availabilityMessage());
    }

    @Test
    void should_ReturnDisabledWorkspace_When_StudentHasNoTeam() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.empty());

        ActivityWorkspace workspace = service.loadWorkspace("ava@tcu.edu", null);

        assertFalse(workspace.canManageActivities());
        assertEquals("You must be assigned to a senior design team before managing WAR activities.", workspace.availabilityMessage());
    }

    @Test
    void should_ReturnDisabledWorkspace_When_NoEligibleWeeksExist() {
        Section section = buildSection(LocalDate.now().plusWeeks(1), LocalDate.now().plusWeeks(2), List.of());
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));

        ActivityWorkspace workspace = service.loadWorkspace("ava@tcu.edu", null);

        assertFalse(workspace.canManageActivities());
        assertEquals("No eligible weeks are available yet.", workspace.availabilityMessage());
    }

    @Test
    void should_CreateActivity_When_RequestIsValid() {
        Section section = buildSection(LocalDate.now().minusWeeks(2), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);
        Activity saved = buildActivity(1L, team, student, currentIsoWeek());

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.save(any(Activity.class))).thenReturn(saved);

        var result = service.createActivity("ava@tcu.edu", new CreateActivityRequest(
            currentIsoWeek(),
            ActivityCategory.DEVELOPMENT,
            "Implement endpoint",
            "Built the WAR workspace endpoint",
            new BigDecimal("4.50"),
            new BigDecimal("5.00"),
            ActivityStatus.DONE
        ));

        assertEquals(1L, result.activityId());
        assertEquals("Implement endpoint", result.plannedActivity());
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void should_UpdateActivity_When_RequestIsValid() {
        Section section = buildSection(LocalDate.now().minusWeeks(2), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);
        Activity existing = buildActivity(1L, team, student, currentIsoWeek());

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.findByActivityIdAndStudentId(1L, 100L)).thenReturn(Optional.of(existing));
        when(activityRepository.save(existing)).thenReturn(existing);

        var result = service.updateActivity("ava@tcu.edu", 1L, new UpdateActivityRequest(
            ActivityCategory.TESTING,
            "Validate forms",
            "Updated frontend validation rules",
            new BigDecimal("2.00"),
            new BigDecimal("2.50"),
            ActivityStatus.UNDER_TESTING
        ));

        assertEquals(ActivityCategory.TESTING, result.category());
        assertEquals("Validate forms", existing.getPlannedActivity());
    }

    @Test
    void should_DeleteActivity_When_StudentOwnsActivity() {
        Section section = buildSection(LocalDate.now().minusWeeks(2), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);
        Activity existing = buildActivity(1L, team, student, currentIsoWeek());

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.findByActivityIdAndStudentId(1L, 100L)).thenReturn(Optional.of(existing));

        service.deleteActivity("ava@tcu.edu", 1L);

        verify(activityRepository).delete(existing);
    }

    @Test
    void should_ThrowInvalidActivity_When_CreateWeekIsFuture() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(3), List.of());
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));

        InvalidActivityException ex = assertThrows(
            InvalidActivityException.class,
            () -> service.createActivity("ava@tcu.edu", new CreateActivityRequest(
                futureIsoWeek(),
                ActivityCategory.DEVELOPMENT,
                "Implement endpoint",
                "Future work",
                BigDecimal.ONE,
                BigDecimal.ONE,
                ActivityStatus.IN_PROGRESS
            ))
        );

        assertEquals("Future weeks cannot be selected.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidActivity_When_CreateWeekIsOutsideAllowedTimeline() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), List.of());
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));

        InvalidActivityException ex = assertThrows(
            InvalidActivityException.class,
            () -> service.createActivity("ava@tcu.edu", new CreateActivityRequest(
                "2020-W01",
                ActivityCategory.DEVELOPMENT,
                "Implement endpoint",
                "Outside timeline",
                BigDecimal.ONE,
                BigDecimal.ONE,
                ActivityStatus.IN_PROGRESS
            ))
        );

        assertEquals("Selected week is not eligible for WAR activity management.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidActivity_When_RequiredFieldsMissing() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));

        InvalidActivityException ex = assertThrows(
            InvalidActivityException.class,
            () -> service.createActivity("ava@tcu.edu", new CreateActivityRequest(
                currentIsoWeek(),
                null,
                "",
                "",
                null,
                null,
                null
            ))
        );

        assertEquals("Activity category is required.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidActivity_When_HoursAreNegative() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));

        InvalidActivityException ex = assertThrows(
            InvalidActivityException.class,
            () -> service.createActivity("ava@tcu.edu", new CreateActivityRequest(
                currentIsoWeek(),
                ActivityCategory.DEVELOPMENT,
                "Implement endpoint",
                "Negative hours",
                new BigDecimal("-1.00"),
                BigDecimal.ONE,
                ActivityStatus.IN_PROGRESS
            ))
        );

        assertEquals("Planned hours must be zero or greater.", ex.getMessage());
    }

    @Test
    void should_ThrowNotFound_When_UpdatingAnotherStudentsActivity() {
        Section section = buildSection(LocalDate.now().minusWeeks(1), LocalDate.now().plusWeeks(1), List.of(currentIsoWeek()));
        User student = buildStudent(100L, section);
        Team team = buildTeam(2001L, "Pulse Analytics", section);

        when(userRepository.findByEmail("ava@tcu.edu")).thenReturn(Optional.of(student));
        when(teamRepository.findByStudentIdWithSection(100L)).thenReturn(Optional.of(team));
        when(activityRepository.findByActivityIdAndStudentId(99L, 100L)).thenReturn(Optional.empty());

        assertThrows(
            ActivityNotFoundException.class,
            () -> service.updateActivity("ava@tcu.edu", 99L, new UpdateActivityRequest(
                ActivityCategory.DEVELOPMENT,
                "Implement endpoint",
                "No access",
                BigDecimal.ONE,
                BigDecimal.ONE,
                ActivityStatus.IN_PROGRESS
            ))
        );
    }

    private Section buildSection(LocalDate startDate, LocalDate endDate, List<String> activeWeeks) {
        Section section = new Section();
        section.setSectionId(1L);
        section.setSectionName("Spring 2026 - Section A");
        section.setStartDate(startDate);
        section.setEndDate(endDate);
        section.setActiveWeeks(activeWeeks);
        return section;
    }

    private User buildStudent(Long id, Section section) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Ava");
        user.setLastName("Johnson");
        user.setEmail("ava@tcu.edu");
        user.setRoles("student");
        user.setEnabled(true);
        user.setSection(section);
        return user;
    }

    private Team buildTeam(Long id, String name, Section section) {
        Team team = new Team();
        team.setTeamId(id);
        team.setTeamName(name);
        team.setSection(section);
        return team;
    }

    private Activity buildActivity(Long id, Team team, User student, String week) {
        Activity activity = new Activity();
        activity.setActivityId(id);
        activity.setTeam(team);
        activity.setStudent(student);
        activity.setWeek(week);
        activity.setCategory(ActivityCategory.DEVELOPMENT);
        activity.setPlannedActivity("Implement endpoint");
        activity.setDescription("Built the WAR workspace endpoint");
        activity.setPlannedHours(new BigDecimal("4.50"));
        activity.setActualHours(new BigDecimal("5.00"));
        activity.setStatus(ActivityStatus.DONE);
        return activity;
    }

    private String currentIsoWeek() {
        LocalDate now = LocalDate.now();
        return "%d-W%02d".formatted(now.get(WeekFields.ISO.weekBasedYear()), now.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    private String previousIsoWeek() {
        LocalDate previous = LocalDate.now().minusWeeks(1);
        return "%d-W%02d".formatted(previous.get(WeekFields.ISO.weekBasedYear()), previous.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }

    private String futureIsoWeek() {
        LocalDate future = LocalDate.now().plusWeeks(1);
        return "%d-W%02d".formatted(future.get(WeekFields.ISO.weekBasedYear()), future.get(WeekFields.ISO.weekOfWeekBasedYear()));
    }
}

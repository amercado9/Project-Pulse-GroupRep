package team.projectpulse.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamInstructorAssignmentException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.TeamInstructorAssignmentInput;
import team.projectpulse.team.dto.TeamInstructorAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamInstructorAssignmentsRequest;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamInstructorAssignmentServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamInstructorAssignmentService service;

    @Test
    void should_ReturnWorkspace_When_SectionExists() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        User noah = buildInstructor(101L, "Noah", "Bennett", "noah@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of(ivy));
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of(noah));

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L))
            .thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy, noah));

        TeamInstructorAssignmentWorkspace workspace = service.loadWorkspace(1L);

        assertEquals(1L, workspace.sectionId());
        assertEquals("Spring 2026 - Section A", workspace.sectionName());
        assertEquals(List.of("Pulse Analytics", "Requirements Hub"), workspace.teams().stream().map(team -> team.teamName()).toList());
        assertEquals(List.of("Noah Bennett", "Ivy Stone"), workspace.instructors().stream().map(instructor -> instructor.fullName()).toList());
    }

    @Test
    void should_ReturnAssignedTeamNamesPerSelectedSection_When_LoadingWorkspace() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        User noah = buildInstructor(101L, "Noah", "Bennett", "noah@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of(ivy));
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of(ivy, noah));

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L))
            .thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy, noah));

        TeamInstructorAssignmentWorkspace workspace = service.loadWorkspace(1L);

        assertEquals(
            List.of("Pulse Analytics", "Requirements Hub"),
            workspace.instructors().get(1).assignedTeamNames()
        );
        assertEquals(List.of("Requirements Hub"), workspace.instructors().getFirst().assignedTeamNames());
    }

    @Test
    void should_ThrowNotFound_When_SectionDoesNotExist() {
        when(sectionRepository.findById(55L)).thenReturn(Optional.empty());

        SectionNotFoundException ex = assertThrows(
            SectionNotFoundException.class,
            () -> service.loadWorkspace(55L)
        );

        assertEquals("No section found with id: 55", ex.getMessage());
    }

    @Test
    void should_SaveAssignments_When_RequestIsValid() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        User noah = buildInstructor(101L, "Noah", "Bennett", "noah@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of(ivy));
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of(noah));
        List<Team> teams = List.of(pulseAnalytics, requirementsHub);
        List<User> instructors = List.of(ivy, noah);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(teams, teams);
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(instructors);

        TeamInstructorAssignmentWorkspace workspace = service.updateAssignments(
            new UpdateTeamInstructorAssignmentsRequest(
                1L,
                List.of(
                    new TeamInstructorAssignmentInput(2001L, List.of(100L, 101L)),
                    new TeamInstructorAssignmentInput(2002L, List.of(101L))
                )
            )
        );

        assertEquals(List.of("Noah Bennett", "Ivy Stone"), workspace.teams().getFirst().assignedInstructors().stream()
            .map(instructor -> instructor.fullName()).toList());
        assertEquals(List.of("Noah"), requirementsHub.getInstructors().stream().map(User::getFirstName).toList());
        assertEquals(List.of("Ivy", "Noah"), pulseAnalytics.getInstructors().stream().map(User::getFirstName).toList());
        verify(teamRepository).saveAll(teams);
    }

    @Test
    void should_AllowInstructorToBeAssignedToMultipleTeams_When_RequestIsValid() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());
        List<Team> teams = List.of(pulseAnalytics, requirementsHub);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(teams, teams);
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        TeamInstructorAssignmentWorkspace workspace = service.updateAssignments(
            new UpdateTeamInstructorAssignmentsRequest(
                1L,
                List.of(
                    new TeamInstructorAssignmentInput(2001L, List.of(100L)),
                    new TeamInstructorAssignmentInput(2002L, List.of(100L))
                )
            )
        );

        assertEquals(List.of("Ivy Stone"), workspace.teams().getFirst().assignedInstructors().stream()
            .map(instructor -> instructor.fullName()).toList());
        assertEquals(List.of("Ivy Stone"), workspace.teams().get(1).assignedInstructors().stream()
            .map(instructor -> instructor.fullName()).toList());
    }

    @Test
    void should_ThrowInvalidArgument_When_NoTeamsExistInSection() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of());
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(new UpdateTeamInstructorAssignmentsRequest(1L, List.of()))
        );

        assertEquals("At least one team must exist in the selected section.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_NoEnabledInstructorsExist() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of());

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(new UpdateTeamInstructorAssignmentsRequest(1L, List.of()))
        );

        assertEquals("At least one enabled instructor must exist before assignments can be saved.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentContainsDuplicateTeamIds() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(
                        new TeamInstructorAssignmentInput(2001L, List.of(100L)),
                        new TeamInstructorAssignmentInput(2001L, List.of(100L))
                    )
                )
            )
        );

        assertEquals("Each team may only appear once in the assignment payload.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentPayloadOmitsTeam() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(2001L, List.of(100L)))
                )
            )
        );

        assertEquals("Assignments must be provided for every team in the selected section.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentContainsNullInstructorId() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(2001L, java.util.Collections.singletonList(null)))
                )
            )
        );

        assertEquals("Assigned instructor ids must not contain null values.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentContainsDuplicateInstructorIds() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(2001L, List.of(100L, 100L)))
                )
            )
        );

        assertEquals("An instructor may only appear once per team assignment.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignedUserIsNotAnEnabledInstructor() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(2001L, List.of(999L)))
                )
            )
        );

        assertEquals("Instructor 999 is not an enabled instructor.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_TeamDoesNotBelongToSection() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(9999L, List.of(100L)))
                )
            )
        );

        assertEquals("Team 9999 does not belong to section 1.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_TeamWouldHaveNoInstructors() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ivy = buildInstructor(100L, "Ivy", "Stone", "ivy@tcu.edu", true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithInstructorsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledInstructorsOrdered()).thenReturn(List.of(ivy));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamInstructorAssignmentsRequest(
                    1L,
                    List.of(new TeamInstructorAssignmentInput(2001L, List.of()))
                )
            )
        );

        assertEquals("Each team in the selected section must be assigned at least one instructor.", ex.getMessage());
    }

    private Section buildSection(Long sectionId, String sectionName) {
        Section section = new Section();
        section.setSectionId(sectionId);
        section.setSectionName(sectionName);
        return section;
    }

    private Team buildTeam(Long teamId, String teamName, Section section, List<User> instructors) {
        Team team = new Team();
        team.setTeamId(teamId);
        team.setTeamName(teamName);
        team.setSection(section);
        team.setInstructors(new LinkedHashSet<>(instructors));
        return team;
    }

    private User buildInstructor(Long id, String firstName, String lastName, String email, boolean enabled) {
        return buildUser(id, firstName, lastName, email, "instructor", enabled);
    }

    private User buildUser(Long id, String firstName, String lastName, String email, String roles, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles(roles);
        user.setEnabled(enabled);
        return user;
    }
}

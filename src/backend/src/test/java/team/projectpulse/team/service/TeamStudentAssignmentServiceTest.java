package team.projectpulse.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.TeamStudentAssignmentInput;
import team.projectpulse.team.dto.TeamStudentAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamStudentAssignmentsRequest;
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
class TeamStudentAssignmentServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamStudentAssignmentService service;

    @Test
    void should_ReturnWorkspace_When_SectionExists() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        User mia = buildStudent(101L, "Mia", "Chen", "mia@tcu.edu", section, true);
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of(mia));
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of(ava));

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(requirementsHub, pulseAnalytics));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava, mia));

        TeamStudentAssignmentWorkspace workspace = service.loadWorkspace(1L);

        assertEquals(1L, workspace.sectionId());
        assertEquals("Spring 2026 - Section A", workspace.sectionName());
        assertEquals(List.of("Pulse Analytics", "Requirements Hub"), workspace.teams().stream().map(team -> team.teamName()).toList());
        assertEquals(List.of("Mia Chen", "Ava Johnson"), workspace.students().stream().map(student -> student.fullName()).toList());
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
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        User mia = buildStudent(101L, "Mia", "Chen", "mia@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of(ava));
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of(mia));
        List<Team> teams = List.of(pulseAnalytics, requirementsHub);
        List<User> students = List.of(ava, mia);

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(teams, teams);
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(students);

        TeamStudentAssignmentWorkspace workspace = service.updateAssignments(
            new UpdateTeamStudentAssignmentsRequest(
                1L,
                List.of(
                    new TeamStudentAssignmentInput(2001L, List.of(101L)),
                    new TeamStudentAssignmentInput(2002L, List.of(100L))
                )
            )
        );

        assertEquals(List.of("Mia Chen"), workspace.teams().getFirst().assignedStudents().stream().map(student -> student.fullName()).toList());
        assertEquals(List.of("Ava Johnson"), workspace.teams().get(1).assignedStudents().stream().map(student -> student.fullName()).toList());
        assertEquals(List.of("Mia"), pulseAnalytics.getStudents().stream().map(User::getFirstName).toList());
        assertEquals(List.of("Ava"), requirementsHub.getStudents().stream().map(User::getFirstName).toList());
        verify(teamRepository).saveAll(teams);
    }

    @Test
    void should_ThrowInvalidArgument_When_SectionIdIsMissing() {
        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(new UpdateTeamStudentAssignmentsRequest(null, List.of()))
        );

        assertEquals("Section is required.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentContainsDuplicateTeamIds() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        User mia = buildStudent(101L, "Mia", "Chen", "mia@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava, mia));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamStudentAssignmentsRequest(
                    1L,
                    List.of(
                        new TeamStudentAssignmentInput(2001L, List.of(100L)),
                        new TeamStudentAssignmentInput(2001L, List.of(101L))
                    )
                )
            )
        );

        assertEquals("Each team may only appear once in the assignment payload.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_AssignmentContainsDuplicateStudentIds() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        User mia = buildStudent(101L, "Mia", "Chen", "mia@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava, mia));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamStudentAssignmentsRequest(
                    1L,
                    List.of(
                        new TeamStudentAssignmentInput(2001L, List.of(100L)),
                        new TeamStudentAssignmentInput(2002L, List.of(100L, 101L))
                    )
                )
            )
        );

        assertEquals("Each student may only be assigned to one team.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_NotEverySectionStudentIsAssigned() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        User mia = buildStudent(101L, "Mia", "Chen", "mia@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());
        Team requirementsHub = buildTeam(2002L, "Requirements Hub", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(pulseAnalytics, requirementsHub));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava, mia));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamStudentAssignmentsRequest(
                    1L,
                    List.of(
                        new TeamStudentAssignmentInput(2001L, List.of(100L)),
                        new TeamStudentAssignmentInput(2002L, List.of())
                    )
                )
            )
        );

        assertEquals("Every eligible student in the selected section must be assigned to exactly one team.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_TeamDoesNotBelongToSection() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamStudentAssignmentsRequest(
                    1L,
                    List.of(new TeamStudentAssignmentInput(9999L, List.of(100L)))
                )
            )
        );

        assertEquals("Team 9999 does not belong to section 1.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_StudentDoesNotBelongToSection() {
        Section section = buildSection(1L, "Spring 2026 - Section A");
        User ava = buildStudent(100L, "Ava", "Johnson", "ava@tcu.edu", section, true);
        Team pulseAnalytics = buildTeam(2001L, "Pulse Analytics", section, List.of());

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(teamRepository.findAllBySectionIdWithStudentsOrdered(1L)).thenReturn(List.of(pulseAnalytics));
        when(userRepository.findEnabledStudentsBySectionId(1L)).thenReturn(List.of(ava));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> service.updateAssignments(
                new UpdateTeamStudentAssignmentsRequest(
                    1L,
                    List.of(new TeamStudentAssignmentInput(2001L, List.of(999L)))
                )
            )
        );

        assertEquals("Student 999 does not belong to section 1.", ex.getMessage());
    }

    private Section buildSection(Long sectionId, String sectionName) {
        Section section = new Section();
        section.setSectionId(sectionId);
        section.setSectionName(sectionName);
        return section;
    }

    private Team buildTeam(Long teamId, String teamName, Section section, List<User> students) {
        Team team = new Team();
        team.setTeamId(teamId);
        team.setTeamName(teamName);
        team.setSection(section);
        team.setStudents(new LinkedHashSet<>(students));
        return team;
    }

    private User buildStudent(
        Long studentId,
        String firstName,
        String lastName,
        String email,
        Section section,
        boolean enabled
    ) {
        User user = new User();
        user.setId(studentId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRoles("student");
        user.setSection(section);
        user.setEnabled(enabled);
        return user;
    }
}

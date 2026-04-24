package team.projectpulse.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.team.domain.InstructorNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.InvalidTeamInstructorAssignmentException;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.domain.StudentNotFoundException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.domain.TeamAlreadyExistsException;
import team.projectpulse.team.domain.TeamNotFoundException;
import team.projectpulse.team.dto.CreateTeamRequest;
import team.projectpulse.team.dto.TeamDetail;
import team.projectpulse.team.dto.TeamInstructorDetail;
import team.projectpulse.team.dto.TeamMemberDetail;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.dto.UpdateTeamRequest;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

    @Test
    void should_NormalizeBlankFiltersAndMapSortedSummary_When_SearchingTeams() {
        Team team = buildTeam();
        when(teamRepository.search(5L, null, "Pulse", null)).thenReturn(List.of(team));

        List<TeamSummary> results = teamService.findTeams(5L, "   ", " Pulse ", "");

        assertEquals(1, results.size());
        TeamSummary summary = results.getFirst();
        assertEquals(10L, summary.teamId());
        assertEquals(2L, summary.sectionId());
        assertEquals("Spring 2026 - Section A", summary.sectionName());
        assertEquals("Pulse Analytics", summary.teamName());
        assertEquals(List.of("Amy Adams", "Zoe Zeta"), summary.teamMemberNames());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), summary.instructorNames());
        verify(teamRepository).search(5L, null, "Pulse", null);
    }

    @Test
    void should_ReturnEmptyList_When_RepositoryFindsNoTeams() {
        when(teamRepository.search(null, null, null, null)).thenReturn(List.of());

        List<TeamSummary> results = teamService.findTeams(null, null, null, null);

        assertEquals(List.of(), results);
    }

    @Test
    void should_KeepNullableFields_When_MappingSummary() {
        Team team = buildTeam();
        team.setTeamDescription(null);
        team.setTeamWebsiteUrl(null);
        when(teamRepository.search(null, null, null, null)).thenReturn(List.of(team));

        TeamSummary summary = teamService.findTeams(null, null, null, null).getFirst();

        assertNull(summary.teamDescription());
        assertNull(summary.teamWebsiteUrl());
    }

    @Test
    void should_ReturnTeamDetail_When_TeamExists() {
        Team team = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(team));

        TeamDetail detail = teamService.findTeamDetail(10L);

        assertEquals(10L, detail.teamId());
        assertEquals(2L, detail.sectionId());
        assertEquals("Spring 2026 - Section A", detail.sectionName());
        assertEquals("Pulse Analytics", detail.teamName());
        assertEquals("Analytics dashboard", detail.teamDescription());
        assertEquals("https://pulse.example.com", detail.teamWebsiteUrl());
        assertEquals(List.of(
            new TeamMemberDetail(2L, "Amy Adams", "amy@tcu.edu"),
            new TeamMemberDetail(1L, "Zoe Zeta", "zoe@tcu.edu")
        ), detail.teamMembers());
        assertEquals(List.of("Amy Adams", "Zoe Zeta"), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), detail.instructorNames());
        verify(teamRepository).findDetailById(10L);
    }

    @Test
    void should_SortTeamMembersAndInstructorsAlphabetically_When_BuildingDetail() {
        Team team = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(team));

        TeamDetail detail = teamService.findTeamDetail(10L);

        assertEquals(List.of("Amy Adams", "Zoe Zeta"), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), detail.instructorNames());
        assertEquals("Amy Adams", detail.teamMembers().getFirst().fullName());
        assertEquals("amy@tcu.edu", detail.teamMembers().getFirst().email());
    }

    @Test
    void should_ThrowNotFoundException_When_TeamDetailIdDoesNotExist() {
        when(teamRepository.findDetailById(999L)).thenReturn(Optional.empty());

        TeamNotFoundException ex = assertThrows(TeamNotFoundException.class, () -> teamService.findTeamDetail(999L));

        assertEquals("No team found with id: 999", ex.getMessage());
    }

    @Test
    void should_CreateTeam_When_RequestIsValid() {
        Section section = buildSection();
        Team savedTeam = new Team();
        savedTeam.setTeamId(77L);
        savedTeam.setSection(section);
        savedTeam.setTeamName("Requirements Hub");
        savedTeam.setTeamDescription("Centralized workspace");
        savedTeam.setTeamWebsiteUrl("https://requirements-hub.example.com");

        when(sectionRepository.findById(2L)).thenReturn(Optional.of(section));
        when(teamRepository.existsByTeamNameIgnoreCase("Requirements Hub")).thenReturn(false);
        when(teamRepository.save(org.mockito.ArgumentMatchers.any(Team.class))).thenReturn(savedTeam);

        TeamDetail detail = teamService.createTeam(new CreateTeamRequest(
            2L,
            " Requirements Hub ",
            " Centralized workspace ",
            " https://requirements-hub.example.com "
        ));

        assertEquals(77L, detail.teamId());
        assertEquals(2L, detail.sectionId());
        assertEquals("Spring 2026 - Section A", detail.sectionName());
        assertEquals("Requirements Hub", detail.teamName());
        assertEquals("Centralized workspace", detail.teamDescription());
        assertEquals("https://requirements-hub.example.com", detail.teamWebsiteUrl());
        assertEquals(List.of(), detail.teamMembers());
        assertEquals(List.of(), detail.teamMemberNames());
        assertEquals(List.of(), detail.teamInstructors());
        assertEquals(List.of(), detail.instructorNames());
        verify(sectionRepository).findById(2L);
        verify(teamRepository).existsByTeamNameIgnoreCase("Requirements Hub");
        verify(teamRepository).save(org.mockito.ArgumentMatchers.any(Team.class));
    }

    @Test
    void should_TrimAndNormalizeOptionalFields_When_CreatingTeam() {
        Section section = buildSection();
        Team savedTeam = new Team();
        savedTeam.setTeamId(88L);
        savedTeam.setSection(section);
        savedTeam.setTeamName("Review Board");
        savedTeam.setTeamDescription(null);
        savedTeam.setTeamWebsiteUrl(null);

        when(sectionRepository.findById(2L)).thenReturn(Optional.of(section));
        when(teamRepository.existsByTeamNameIgnoreCase("Review Board")).thenReturn(false);
        when(teamRepository.save(org.mockito.ArgumentMatchers.any(Team.class))).thenReturn(savedTeam);

        TeamDetail detail = teamService.createTeam(new CreateTeamRequest(2L, " Review Board ", "   ", "   "));

        assertNull(detail.teamDescription());
        assertNull(detail.teamWebsiteUrl());
    }

    @Test
    void should_ThrowNotFoundException_When_SectionDoesNotExist() {
        when(sectionRepository.findById(22L)).thenReturn(Optional.empty());

        SectionNotFoundException ex = assertThrows(
            SectionNotFoundException.class,
            () -> teamService.createTeam(new CreateTeamRequest(22L, "Requirements Hub", null, null))
        );

        assertEquals("No section found with id: 22", ex.getMessage());
    }

    @Test
    void should_ThrowConflict_When_TeamNameAlreadyExistsIgnoringCase() {
        Section section = buildSection();
        when(sectionRepository.findById(2L)).thenReturn(Optional.of(section));
        when(teamRepository.existsByTeamNameIgnoreCase("pulse analytics")).thenReturn(true);

        TeamAlreadyExistsException ex = assertThrows(
            TeamAlreadyExistsException.class,
            () -> teamService.createTeam(new CreateTeamRequest(2L, " pulse analytics ", null, null))
        );

        assertEquals("Team already exists with name: pulse analytics", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_TeamNameIsBlank() {
        InvalidTeamException ex = assertThrows(
            InvalidTeamException.class,
            () -> teamService.createTeam(new CreateTeamRequest(2L, "   ", null, null))
        );

        assertEquals("Team name is required.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_WebsiteUrlIsInvalid() {
        InvalidTeamException ex = assertThrows(
            InvalidTeamException.class,
            () -> teamService.createTeam(new CreateTeamRequest(2L, "Requirements Hub", null, "not-a-url"))
        );

        assertEquals("Team website URL must start with http:// or https://.", ex.getMessage());
    }

    @Test
    void should_ReturnCreatedTeamDetailWithEmptyMembersAndInstructors_When_TeamIsCreated() {
        Section section = buildSection();
        Team savedTeam = new Team();
        savedTeam.setTeamId(99L);
        savedTeam.setSection(section);
        savedTeam.setTeamName("New Team");

        when(sectionRepository.findById(2L)).thenReturn(Optional.of(section));
        when(teamRepository.existsByTeamNameIgnoreCase("New Team")).thenReturn(false);
        when(teamRepository.save(org.mockito.ArgumentMatchers.any(Team.class))).thenReturn(savedTeam);

        TeamDetail detail = teamService.createTeam(new CreateTeamRequest(2L, "New Team", null, null));

        assertEquals(List.of(), detail.teamMemberNames());
        assertEquals(List.of(), detail.teamInstructors());
        assertEquals(List.of(), detail.instructorNames());
        assertEquals(List.of(), detail.teamMembers());
    }

    @Test
    void should_UpdateTeam_When_RequestIsValid() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("Requirements Hub", 10L)).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.updateTeam(10L, new UpdateTeamRequest(
            " Requirements Hub ",
            " Centralized workspace ",
            " https://requirements-hub.example.com "
        ));

        assertEquals("Requirements Hub", detail.teamName());
        assertEquals("Centralized workspace", detail.teamDescription());
        assertEquals("https://requirements-hub.example.com", detail.teamWebsiteUrl());
        assertEquals(List.of(
            new TeamMemberDetail(2L, "Amy Adams", "amy@tcu.edu"),
            new TeamMemberDetail(1L, "Zoe Zeta", "zoe@tcu.edu")
        ), detail.teamMembers());
        assertEquals(List.of("Amy Adams", "Zoe Zeta"), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), detail.instructorNames());
        verify(teamRepository).findDetailById(10L);
        verify(teamRepository).existsByTeamNameIgnoreCaseAndTeamIdNot("Requirements Hub", 10L);
        verify(teamRepository).save(existingTeam);
    }

    @Test
    void should_TrimAndNormalizeOptionalFields_When_UpdatingTeam() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("Pulse Analytics", 10L)).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.updateTeam(10L, new UpdateTeamRequest(" Pulse Analytics ", "   ", "   "));

        assertNull(detail.teamDescription());
        assertNull(detail.teamWebsiteUrl());
    }

    @Test
    void should_AllowSameName_When_UpdatingCurrentTeamWithoutChangingUniqueness() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("Pulse Analytics", 10L)).thenReturn(false);
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.updateTeam(10L, new UpdateTeamRequest(
            "Pulse Analytics",
            "Updated description",
            "https://pulse.example.com"
        ));

        assertEquals("Pulse Analytics", detail.teamName());
        assertEquals("Updated description", detail.teamDescription());
    }

    @Test
    void should_ThrowNotFoundException_When_UpdatingMissingTeam() {
        when(teamRepository.findDetailById(404L)).thenReturn(Optional.empty());

        TeamNotFoundException ex = assertThrows(
            TeamNotFoundException.class,
            () -> teamService.updateTeam(404L, new UpdateTeamRequest("Requirements Hub", null, null))
        );

        assertEquals("No team found with id: 404", ex.getMessage());
    }

    @Test
    void should_ThrowConflict_When_OtherTeamUsesRequestedNameIgnoringCase() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("requirements hub", 10L)).thenReturn(true);

        TeamAlreadyExistsException ex = assertThrows(
            TeamAlreadyExistsException.class,
            () -> teamService.updateTeam(10L, new UpdateTeamRequest(" requirements hub ", null, null))
        );

        assertEquals("Team already exists with name: requirements hub", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_UpdateTeamNameIsBlank() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));

        InvalidTeamException ex = assertThrows(
            InvalidTeamException.class,
            () -> teamService.updateTeam(10L, new UpdateTeamRequest("   ", null, null))
        );

        assertEquals("Team name is required.", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_UpdateWebsiteUrlIsInvalid() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));

        InvalidTeamException ex = assertThrows(
            InvalidTeamException.class,
            () -> teamService.updateTeam(10L, new UpdateTeamRequest("Requirements Hub", null, "not-a-url"))
        );

        assertEquals("Team website URL must start with http:// or https://.", ex.getMessage());
    }

    @Test
    void should_DeleteTeam_When_TeamExists() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));

        teamService.deleteTeam(10L);

        verify(teamRepository).findDetailById(10L);
        verify(teamRepository).delete(existingTeam);
    }

    @Test
    void should_ThrowNotFoundException_When_DeletingMissingTeam() {
        when(teamRepository.findDetailById(404L)).thenReturn(Optional.empty());

        TeamNotFoundException ex = assertThrows(
            TeamNotFoundException.class,
            () -> teamService.deleteTeam(404L)
        );

        assertEquals("No team found with id: 404", ex.getMessage());
        verify(teamRepository, never()).delete(org.mockito.ArgumentMatchers.any(Team.class));
    }

    @Test
    void should_LoadExistingTeamBeforeDelete_When_DeletingTeam() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));

        teamService.deleteTeam(10L);

        verify(teamRepository).findDetailById(10L);
        verify(teamRepository).delete(existingTeam);
    }

    @Test
    void should_RemoveStudentFromTeam_When_RequestIsValid() {
        Team existingTeam = buildTeam();
        User student = existingTeam.getStudents().stream()
            .filter(teamStudent -> teamStudent.getId().equals(1L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeStudentFromTeam(10L, 1L);

        assertEquals(List.of(new TeamMemberDetail(2L, "Amy Adams", "amy@tcu.edu")), detail.teamMembers());
        assertEquals(List.of("Amy Adams"), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), detail.instructorNames());
        verify(teamRepository).findDetailById(10L);
        verify(userRepository).findById(1L);
        verify(teamRepository).save(existingTeam);
    }

    @Test
    void should_RemoveLastStudentFromTeam_When_LastMemberIsRemoved() {
        Team existingTeam = buildTeam();
        User onlyStudent = new User();
        onlyStudent.setId(5L);
        onlyStudent.setFirstName("Ava");
        onlyStudent.setLastName("Johnson");
        onlyStudent.setEmail("ava@tcu.edu");
        existingTeam.setStudents(new LinkedHashSet<>(List.of(onlyStudent)));

        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(5L)).thenReturn(Optional.of(onlyStudent));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeStudentFromTeam(10L, 5L);

        assertEquals(List.of(), detail.teamMembers());
        assertEquals(List.of(), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
    }

    @Test
    void should_ThrowTeamNotFound_When_RemovingStudentFromMissingTeam() {
        when(teamRepository.findDetailById(404L)).thenReturn(Optional.empty());

        TeamNotFoundException ex = assertThrows(
            TeamNotFoundException.class,
            () -> teamService.removeStudentFromTeam(404L, 1L)
        );

        assertEquals("No team found with id: 404", ex.getMessage());
    }

    @Test
    void should_ThrowStudentNotFound_When_StudentIdDoesNotExist() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        StudentNotFoundException ex = assertThrows(
            StudentNotFoundException.class,
            () -> teamService.removeStudentFromTeam(10L, 404L)
        );

        assertEquals("No student found with id: 404", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidArgument_When_StudentIsNotAssignedToTeam() {
        Team existingTeam = buildTeam();
        User student = new User();
        student.setId(55L);
        student.setFirstName("Mia");
        student.setLastName("Chen");
        student.setEmail("mia@tcu.edu");
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(55L)).thenReturn(Optional.of(student));

        InvalidTeamStudentAssignmentException ex = assertThrows(
            InvalidTeamStudentAssignmentException.class,
            () -> teamService.removeStudentFromTeam(10L, 55L)
        );

        assertEquals("Student 55 is not assigned to team 10.", ex.getMessage());
    }

    @Test
    void should_PreserveSectionAndInstructors_When_RemovingStudent() {
        Team existingTeam = buildTeam();
        User student = existingTeam.getStudents().stream()
            .filter(teamStudent -> teamStudent.getId().equals(1L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeStudentFromTeam(10L, 1L);

        assertEquals(2L, detail.sectionId());
        assertEquals("Spring 2026 - Section A", detail.sectionName());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone", "Noah Bennett"), detail.instructorNames());
    }

    @Test
    void should_ReturnTeamDetailWithTeamMembersAndNames_When_RemovalSucceeds() {
        Team existingTeam = buildTeam();
        User student = existingTeam.getStudents().stream()
            .filter(teamStudent -> teamStudent.getId().equals(2L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeStudentFromTeam(10L, 2L);

        assertEquals(List.of(new TeamMemberDetail(1L, "Zoe Zeta", "zoe@tcu.edu")), detail.teamMembers());
        assertEquals(List.of("Zoe Zeta"), detail.teamMemberNames());
        assertEquals(List.of(
            new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu"),
            new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")
        ), detail.teamInstructors());
    }

    @Test
    void should_RemoveInstructorFromTeam_When_InstructorIsAssigned() {
        Team existingTeam = buildTeam();
        User instructor = existingTeam.getInstructors().stream()
            .filter(teamInstructor -> teamInstructor.getId().equals(3L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(3L)).thenReturn(Optional.of(instructor));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeInstructorFromTeam(10L, 3L);

        assertEquals(List.of(new TeamInstructorDetail(4L, "Ivy Stone", "ivy@tcu.edu")), detail.teamInstructors());
        assertEquals(List.of("Ivy Stone"), detail.instructorNames());
        assertEquals(List.of("Amy Adams", "Zoe Zeta"), detail.teamMemberNames());
        verify(teamRepository).findDetailById(10L);
        verify(userRepository).findById(3L);
        verify(teamRepository).save(existingTeam);
    }

    @Test
    void should_ReturnUpdatedDetail_When_RemovingInstructor() {
        Team existingTeam = buildTeam();
        User instructor = existingTeam.getInstructors().stream()
            .filter(teamInstructor -> teamInstructor.getId().equals(4L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(4L)).thenReturn(Optional.of(instructor));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeInstructorFromTeam(10L, 4L);

        assertEquals(10L, detail.teamId());
        assertEquals("Pulse Analytics", detail.teamName());
        assertEquals(List.of(new TeamInstructorDetail(3L, "Noah Bennett", "noah@tcu.edu")), detail.teamInstructors());
        assertEquals(List.of("Noah Bennett"), detail.instructorNames());
    }

    @Test
    void should_AllowRemovingLastInstructor_When_NoOtherInstructorsRemain() {
        Team existingTeam = buildTeam();
        User onlyInstructor = existingTeam.getInstructors().stream()
            .filter(teamInstructor -> teamInstructor.getId().equals(4L))
            .findFirst()
            .orElseThrow();
        existingTeam.setInstructors(new LinkedHashSet<>(List.of(onlyInstructor)));

        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(4L)).thenReturn(Optional.of(onlyInstructor));
        when(teamRepository.save(existingTeam)).thenReturn(existingTeam);

        TeamDetail detail = teamService.removeInstructorFromTeam(10L, 4L);

        assertEquals(List.of(), detail.teamInstructors());
        assertEquals(List.of(), detail.instructorNames());
    }

    @Test
    void should_ThrowTeamNotFoundException_When_RemovingInstructorFromMissingTeam() {
        when(teamRepository.findDetailById(404L)).thenReturn(Optional.empty());

        TeamNotFoundException ex = assertThrows(
            TeamNotFoundException.class,
            () -> teamService.removeInstructorFromTeam(404L, 3L)
        );

        assertEquals("No team found with id: 404", ex.getMessage());
    }

    @Test
    void should_ThrowInstructorNotFoundException_When_InstructorDoesNotExist() {
        Team existingTeam = buildTeam();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        InstructorNotFoundException ex = assertThrows(
            InstructorNotFoundException.class,
            () -> teamService.removeInstructorFromTeam(10L, 404L)
        );

        assertEquals("No instructor found with id: 404", ex.getMessage());
    }

    @Test
    void should_ThrowInstructorNotFoundException_When_UserIsNotInstructor() {
        Team existingTeam = buildTeam();
        User student = existingTeam.getStudents().stream()
            .filter(teamStudent -> teamStudent.getId().equals(1L))
            .findFirst()
            .orElseThrow();
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        InstructorNotFoundException ex = assertThrows(
            InstructorNotFoundException.class,
            () -> teamService.removeInstructorFromTeam(10L, 1L)
        );

        assertEquals("No instructor found with id: 1", ex.getMessage());
    }

    @Test
    void should_ThrowInvalidTeamInstructorAssignmentException_When_InstructorIsNotAssignedToTeam() {
        Team existingTeam = buildTeam();
        User instructor = new User();
        instructor.setId(55L);
        instructor.setFirstName("Maya");
        instructor.setLastName("Lee");
        instructor.setEmail("maya@tcu.edu");
        instructor.setRoles("instructor");
        when(teamRepository.findDetailById(10L)).thenReturn(Optional.of(existingTeam));
        when(userRepository.findById(55L)).thenReturn(Optional.of(instructor));

        InvalidTeamInstructorAssignmentException ex = assertThrows(
            InvalidTeamInstructorAssignmentException.class,
            () -> teamService.removeInstructorFromTeam(10L, 55L)
        );

        assertEquals("Instructor 55 is not assigned to team 10.", ex.getMessage());
    }

    private Team buildTeam() {
        Section section = buildSection();

        User student1 = new User();
        student1.setId(1L);
        student1.setFirstName("Zoe");
        student1.setLastName("Zeta");
        student1.setEmail("zoe@tcu.edu");

        User student2 = new User();
        student2.setId(2L);
        student2.setFirstName("Amy");
        student2.setLastName("Adams");
        student2.setEmail("amy@tcu.edu");

        User instructor1 = new User();
        instructor1.setId(3L);
        instructor1.setFirstName("Noah");
        instructor1.setLastName("Bennett");
        instructor1.setEmail("noah@tcu.edu");
        instructor1.setRoles("instructor");

        User instructor2 = new User();
        instructor2.setId(4L);
        instructor2.setFirstName("Ivy");
        instructor2.setLastName("Stone");
        instructor2.setEmail("ivy@tcu.edu");
        instructor2.setRoles("instructor");

        Team team = new Team();
        team.setTeamId(10L);
        team.setSection(section);
        team.setTeamName("Pulse Analytics");
        team.setTeamDescription("Analytics dashboard");
        team.setTeamWebsiteUrl("https://pulse.example.com");
        team.setStudents(new LinkedHashSet<>(List.of(student1, student2)));
        team.setInstructors(new LinkedHashSet<>(List.of(instructor1, instructor2)));
        return team;
    }

    private Section buildSection() {
        Section section = new Section();
        section.setSectionId(2L);
        section.setSectionName("Spring 2026 - Section A");
        return section;
    }
}

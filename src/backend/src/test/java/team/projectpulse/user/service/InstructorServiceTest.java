package team.projectpulse.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.dto.InstructorSummary;
import team.projectpulse.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private InstructorService instructorService;

    // ── Search ────────────────────────────────────────────────────────────────

    @Test
    void should_ReturnSortedSummaries_When_SearchingWithNoFilters() {
        User ivy = buildInstructor(200L, "Ivy", "Stone", true);
        User noah = buildInstructor(201L, "Noah", "Bennett", true);

        when(userRepository.searchInstructors(null, null, null, null)).thenReturn(List.of(ivy, noah));

        Team pulseTeam = buildTeam("Pulse Analytics", 2026);
        when(teamRepository.findAllByInstructorId(200L)).thenReturn(List.of(pulseTeam));
        when(teamRepository.findAllByInstructorId(201L)).thenReturn(List.of());

        List<InstructorSummary> results = instructorService.searchInstructors(null, null, null, null);

        // Sorted: 2026 (Ivy Stone) first, then null year (Noah Bennett)
        assertEquals(2, results.size());
        assertEquals("Stone", results.get(0).lastName());
        assertEquals(2026, results.get(0).academicYear());
        assertEquals(List.of("Pulse Analytics"), results.get(0).teamNames());
        assertEquals("Bennett", results.get(1).lastName());
        assertNull(results.get(1).academicYear());
    }

    @Test
    void should_SortByAcademicYearDescThenLastNameAsc_When_MultipleInstructors() {
        User charlie = buildInstructor(1L, "Charlie", "Adams", true);
        User alice = buildInstructor(2L, "Alice", "Zane", true);
        User bob = buildInstructor(3L, "Bob", "Adams", true);

        when(userRepository.searchInstructors(null, null, null, null)).thenReturn(List.of(charlie, alice, bob));

        when(teamRepository.findAllByInstructorId(1L)).thenReturn(List.of(buildTeam("TeamA", 2025)));
        when(teamRepository.findAllByInstructorId(2L)).thenReturn(List.of(buildTeam("TeamB", 2026)));
        when(teamRepository.findAllByInstructorId(3L)).thenReturn(List.of(buildTeam("TeamC", 2026)));

        List<InstructorSummary> results = instructorService.searchInstructors(null, null, null, null);

        assertEquals(3, results.size());
        // 2026 first: Adams (Bob) before Zane (Alice) alphabetically
        assertEquals("Adams", results.get(0).lastName());
        assertEquals(2026, results.get(0).academicYear());
        assertEquals("Zane", results.get(1).lastName());
        assertEquals(2026, results.get(1).academicYear());
        // 2025 last
        assertEquals("Adams", results.get(2).lastName());
        assertEquals(2025, results.get(2).academicYear());
    }

    @Test
    void should_NormalizeBlankFilters_When_CallingRepository() {
        when(userRepository.searchInstructors(null, "Stone", null, null)).thenReturn(List.of());

        instructorService.searchInstructors("  ", "Stone", null, "");

        verify(userRepository).searchInstructors(null, "Stone", null, null);
    }

    @Test
    void should_ReturnEmptyList_When_NoInstructorsMatch() {
        when(userRepository.searchInstructors(any(), any(), any(), any())).thenReturn(List.of());

        List<InstructorSummary> results = instructorService.searchInstructors("Unknown", null, null, null);

        assertEquals(List.of(), results);
    }

    @Test
    void should_SetAcademicYearFromMostRecentTeamSection_When_InstructorHasMultipleTeams() {
        User instructor = buildInstructor(1L, "Ivy", "Stone", true);
        when(userRepository.searchInstructors(null, null, null, null)).thenReturn(List.of(instructor));

        // findAllByInstructorId returns most recent first (per query order: startDate DESC)
        when(teamRepository.findAllByInstructorId(1L)).thenReturn(List.of(
                buildTeam("Spring Team", 2026),
                buildTeam("Fall Team", 2025)
        ));

        List<InstructorSummary> results = instructorService.searchInstructors(null, null, null, null);

        assertEquals(2026, results.get(0).academicYear());
        assertEquals(List.of("Spring Team", "Fall Team"), results.get(0).teamNames());
    }

    @Test
    void should_SetNullAcademicYear_When_InstructorHasNoTeam() {
        User instructor = buildInstructor(1L, "Ivy", "Stone", true);
        when(userRepository.searchInstructors(null, null, null, null)).thenReturn(List.of(instructor));
        when(teamRepository.findAllByInstructorId(1L)).thenReturn(List.of());

        List<InstructorSummary> results = instructorService.searchInstructors(null, null, null, null);

        assertNull(results.get(0).academicYear());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private User buildInstructor(Long id, String firstName, String lastName, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@tcu.edu");
        user.setPassword("encoded");
        user.setRoles("instructor");
        user.setEnabled(enabled);
        return user;
    }

    private Team buildTeam(String teamName, int startYear) {
        Section section = new Section();
        section.setSectionName("Section " + startYear);
        section.setStartDate(LocalDate.of(startYear, 1, 15));
        section.setEndDate(LocalDate.of(startYear, 5, 10));

        Team team = new Team();
        team.setTeamName(teamName);
        team.setSection(section);
        team.setStudents(new LinkedHashSet<>());
        team.setInstructors(new LinkedHashSet<>());
        return team;
    }
}

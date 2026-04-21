package team.projectpulse.team.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;

import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

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

    private Team buildTeam() {
        Section section = new Section();
        section.setSectionId(2L);
        section.setSectionName("Spring 2026 - Section A");

        User student1 = new User();
        student1.setFirstName("Zoe");
        student1.setLastName("Zeta");

        User student2 = new User();
        student2.setFirstName("Amy");
        student2.setLastName("Adams");

        User instructor1 = new User();
        instructor1.setFirstName("Noah");
        instructor1.setLastName("Bennett");

        User instructor2 = new User();
        instructor2.setFirstName("Ivy");
        instructor2.setLastName("Stone");

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
}

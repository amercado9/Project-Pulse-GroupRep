package team.projectpulse.team.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.service.TeamService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeamControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamService teamService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnTeamsWrappedInResult_When_AdminRequestsTeams() throws Exception {
        when(teamService.findTeams(null, null, "Pulse", null))
            .thenReturn(List.of(new TeamSummary(1L, 1L, "Spring 2026 - Section A", "Pulse Analytics", "desc", "https://pulse.example.com", List.of("Ava Johnson"), List.of("Ivy Stone"))));

        mockMvc.perform(get("/api/v1/teams").param("teamName", "Pulse"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].teamName").value("Pulse Analytics"));

        verify(teamService).findTeams(null, null, "Pulse", null);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_AllowInstructor_When_RequestingTeams() throws Exception {
        when(teamService.findTeams(null, "Spring", null, "Ivy")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/teams")
                .param("sectionName", "Spring")
                .param("instructor", "Ivy"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());

        verify(teamService).findTeams(null, "Spring", null, "Ivy");
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsTeams() throws Exception {
        mockMvc.perform(get("/api/v1/teams"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_RequestIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/teams"))
            .andExpect(status().isUnauthorized());
    }
}

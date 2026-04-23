package team.projectpulse.team.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.domain.StudentNotFoundException;
import team.projectpulse.team.domain.TeamAlreadyExistsException;
import team.projectpulse.team.domain.TeamNotFoundException;
import team.projectpulse.team.dto.TeamDetail;
import team.projectpulse.team.dto.TeamMemberDetail;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.service.TeamService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnTeamDetailWrappedInResult_When_AdminRequestsTeam() throws Exception {
        when(teamService.findTeamDetail(1L))
            .thenReturn(new TeamDetail(
                1L,
                1L,
                "Spring 2026 - Section A",
                "Pulse Analytics",
                "desc",
                "https://pulse.example.com",
                List.of(new TeamMemberDetail(100L, "Ava Johnson", "ava@tcu.edu")),
                List.of("Ava Johnson"),
                List.of("Ivy Stone")
            ));

        mockMvc.perform(get("/api/v1/teams/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.teamName").value("Pulse Analytics"))
            .andExpect(jsonPath("$.data.sectionName").value("Spring 2026 - Section A"));

        verify(teamService).findTeamDetail(1L);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_AllowInstructor_When_RequestingTeamDetail() throws Exception {
        when(teamService.findTeamDetail(2L))
            .thenReturn(new TeamDetail(
                2L,
                1L,
                "Spring 2026 - Section A",
                "Review Board",
                null,
                null,
                List.of(),
                List.of(),
                List.of()
            ));

        mockMvc.perform(get("/api/v1/teams/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.teamId").value(2));

        verify(teamService).findTeamDetail(2L);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsTeamDetail() throws Exception {
        mockMvc.perform(get("/api/v1/teams/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_RequestIsUnauthenticated_ForTeamDetail() throws Exception {
        mockMvc.perform(get("/api/v1/teams/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_TeamDoesNotExist() throws Exception {
        when(teamService.findTeamDetail(eq(999L))).thenThrow(new TeamNotFoundException(999L));

        mockMvc.perform(get("/api/v1/teams/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No team found with id: 999"));

        verify(teamService).findTeamDetail(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_CreateTeamWrappedInResult_When_AdminRequestsCreate() throws Exception {
        when(teamService.createTeam(org.mockito.ArgumentMatchers.any()))
            .thenReturn(new TeamDetail(
                10L,
                2L,
                "Spring 2026 - Section A",
                "Requirements Hub",
                "desc",
                "https://requirements-hub.example.com",
                List.of(),
                List.of(),
                List.of()
            ));

        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sectionId": 2,
                      "teamName": "Requirements Hub",
                      "teamDescription": "desc",
                      "teamWebsiteUrl": "https://requirements-hub.example.com"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Team created successfully."))
            .andExpect(jsonPath("$.data.teamId").value(10))
            .andExpect(jsonPath("$.data.teamName").value("Requirements Hub"))
            .andExpect(jsonPath("$.data.teamMemberNames").isArray())
            .andExpect(jsonPath("$.data.instructorNames").isArray());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsCreate() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":2,\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsCreate() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":2,\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedRequestCreatesTeam() throws Exception {
        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":2,\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnConflict_When_TeamNameAlreadyExists() throws Exception {
        when(teamService.createTeam(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new TeamAlreadyExistsException("Requirements Hub"));

        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":2,\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("Team already exists with name: Requirements Hub"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnBadRequest_When_CreateRequestIsInvalid() throws Exception {
        when(teamService.createTeam(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new InvalidTeamException("Team name is required."));

        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":2,\"teamName\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Team name is required."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_SectionIdDoesNotExist() throws Exception {
        when(teamService.createTeam(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new SectionNotFoundException(999L));

        mockMvc.perform(post("/api/v1/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"sectionId\":999,\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No section found with id: 999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_UpdateTeamWrappedInResult_When_AdminRequestsUpdate() throws Exception {
        when(teamService.updateTeam(eq(10L), org.mockito.ArgumentMatchers.any()))
            .thenReturn(new TeamDetail(
                10L,
                2L,
                "Spring 2026 - Section A",
                "Requirements Hub",
                "Centralized workspace",
                "https://requirements-hub.example.com",
                List.of(),
                List.of(),
                List.of("Ivy Stone")
            ));

        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "teamName": "Requirements Hub",
                      "teamDescription": "Centralized workspace",
                      "teamWebsiteUrl": "https://requirements-hub.example.com"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Team updated successfully."))
            .andExpect(jsonPath("$.data.teamId").value(10))
            .andExpect(jsonPath("$.data.teamName").value("Requirements Hub"));

        verify(teamService).updateTeam(eq(10L), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsUpdate() throws Exception {
        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedRequestUpdatesTeam() throws Exception {
        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_UpdatedTeamDoesNotExist() throws Exception {
        when(teamService.updateTeam(eq(999L), org.mockito.ArgumentMatchers.any()))
            .thenThrow(new TeamNotFoundException(999L));

        mockMvc.perform(put("/api/v1/teams/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No team found with id: 999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnConflict_When_UpdatedTeamNameAlreadyExists() throws Exception {
        when(teamService.updateTeam(eq(10L), org.mockito.ArgumentMatchers.any()))
            .thenThrow(new TeamAlreadyExistsException("Requirements Hub"));

        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"Requirements Hub\"}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("Team already exists with name: Requirements Hub"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnBadRequest_When_UpdateRequestIsInvalid() throws Exception {
        when(teamService.updateTeam(eq(10L), org.mockito.ArgumentMatchers.any()))
            .thenThrow(new InvalidTeamException("Team name is required."));

        mockMvc.perform(put("/api/v1/teams/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamName\":\"   \"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Team name is required."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_RemoveStudentFromTeam_When_AdminRequestsRemoval() throws Exception {
        when(teamService.removeStudentFromTeam(10L, 100L))
            .thenReturn(new TeamDetail(
                10L,
                2L,
                "Spring 2026 - Section A",
                "Requirements Hub",
                "Centralized workspace",
                "https://requirements-hub.example.com",
                List.of(),
                List.of(),
                List.of("Ivy Stone")
            ));

        mockMvc.perform(delete("/api/v1/teams/10/students/100"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Student removed from team successfully."))
            .andExpect(jsonPath("$.data.teamId").value(10))
            .andExpect(jsonPath("$.data.teamMembers").isArray())
            .andExpect(jsonPath("$.data.teamMemberNames").isArray());

        verify(teamService).removeStudentFromTeam(10L, 100L);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsStudentRemoval() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/10/students/100"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsStudentRemoval() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/10/students/100"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedRequestRemovesStudent() throws Exception {
        mockMvc.perform(delete("/api/v1/teams/10/students/100"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_RemovingStudentFromMissingTeam() throws Exception {
        when(teamService.removeStudentFromTeam(999L, 100L))
            .thenThrow(new TeamNotFoundException(999L));

        mockMvc.perform(delete("/api/v1/teams/999/students/100"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No team found with id: 999"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_RemovedStudentDoesNotExist() throws Exception {
        when(teamService.removeStudentFromTeam(10L, 404L))
            .thenThrow(new StudentNotFoundException(404L));

        mockMvc.perform(delete("/api/v1/teams/10/students/404"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No student found with id: 404"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnBadRequest_When_StudentIsNotAssignedToTeam() throws Exception {
        when(teamService.removeStudentFromTeam(10L, 100L))
            .thenThrow(new InvalidTeamStudentAssignmentException("Student 100 is not assigned to team 10."));

        mockMvc.perform(delete("/api/v1/teams/10/students/100"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Student 100 is not assigned to team 10."));
    }
}

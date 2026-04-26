package team.projectpulse.team.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.config.ControllerTestSecurityConfig;
import team.projectpulse.config.SecurityConfig;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.dto.StudentAssignmentCandidate;
import team.projectpulse.team.dto.TeamStudentAssignmentTeam;
import team.projectpulse.team.dto.TeamStudentAssignmentWorkspace;
import team.projectpulse.team.service.TeamStudentAssignmentService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TeamStudentAssignmentController.class)
@Import({TeamExceptionHandler.class, SecurityConfig.class, ControllerTestSecurityConfig.class})
@ActiveProfiles("test")
class TeamStudentAssignmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TeamStudentAssignmentService teamStudentAssignmentService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_LoadWorkspace_When_AdminRequestsAssignments() throws Exception {
        when(teamStudentAssignmentService.loadWorkspace(1L)).thenReturn(buildWorkspace());

        mockMvc.perform(get("/api/v1/team-student-assignments").param("sectionId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.sectionName").value("Spring 2026 - Section A"))
            .andExpect(jsonPath("$.data.teams[0].teamName").value("Pulse Analytics"))
            .andExpect(jsonPath("$.data.students[0].fullName").value("Ava Johnson"));

        verify(teamStudentAssignmentService).loadWorkspace(1L);
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/team-student-assignments").param("sectionId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/team-student-assignments").param("sectionId", "1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserRequestsAssignments() throws Exception {
        mockMvc.perform(get("/api/v1/team-student-assignments").param("sectionId", "1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_SaveAssignments_When_AdminConfirmsAssignments() throws Exception {
        when(teamStudentAssignmentService.updateAssignments(org.mockito.ArgumentMatchers.any())).thenReturn(buildWorkspace());

        mockMvc.perform(put("/api/v1/team-student-assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sectionId": 1,
                      "assignments": [
                        { "teamId": 2001, "studentIds": [100] }
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Student team assignments saved successfully."))
            .andExpect(jsonPath("$.data.teams[0].assignedStudents[0].studentId").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnBadRequest_When_AssignmentPayloadIsInvalid() throws Exception {
        when(teamStudentAssignmentService.updateAssignments(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new InvalidTeamStudentAssignmentException("Every eligible student in the selected section must be assigned to exactly one team."));

        mockMvc.perform(put("/api/v1/team-student-assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "sectionId": 1,
                      "assignments": [
                        { "teamId": 2001, "studentIds": [] }
                      ]
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Every eligible student in the selected section must be assigned to exactly one team."));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_SectionDoesNotExist() throws Exception {
        when(teamStudentAssignmentService.loadWorkspace(999L)).thenThrow(new SectionNotFoundException(999L));

        mockMvc.perform(get("/api/v1/team-student-assignments").param("sectionId", "999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No section found with id: 999"));
    }

    private TeamStudentAssignmentWorkspace buildWorkspace() {
        return new TeamStudentAssignmentWorkspace(
            1L,
            "Spring 2026 - Section A",
            List.of(
                new TeamStudentAssignmentTeam(
                    2001L,
                    "Pulse Analytics",
                    List.of(new StudentAssignmentCandidate(100L, "Ava Johnson", "ava@tcu.edu"))
                )
            ),
            List.of(new StudentAssignmentCandidate(100L, "Ava Johnson", "ava@tcu.edu"))
        );
    }
}

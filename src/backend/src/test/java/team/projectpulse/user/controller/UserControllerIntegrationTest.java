package team.projectpulse.user.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.config.ControllerTestSecurityConfig;
import team.projectpulse.config.SecurityConfig;
import team.projectpulse.user.domain.StudentNotFoundException;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.service.StudentService;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({UserExceptionHandler.class, SecurityConfig.class, ControllerTestSecurityConfig.class})
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService studentService;

    // ── GET /users/students ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnStudentList_When_AdminRequestsStudents() throws Exception {
        when(studentService.findAllStudents()).thenReturn(List.of(
            new StudentSummary(1003L, "Ava", "Johnson", "ava.johnson@tcu.edu", true, 1L, "Spring 2026 - Section A"),
            new StudentSummary(1004L, "Liam", "Parker", "liam.parker@tcu.edu", true, 1L, "Spring 2026 - Section A")
        ));

        mockMvc.perform(get("/api/v1/users/students"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data[0].email").value("ava.johnson@tcu.edu"))
            .andExpect(jsonPath("$.data[1].email").value("liam.parker@tcu.edu"));

        verify(studentService).findAllStudents();
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsStudentList() throws Exception {
        mockMvc.perform(get("/api/v1/users/students"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedRequestsStudentList() throws Exception {
        mockMvc.perform(get("/api/v1/users/students"))
            .andExpect(status().isUnauthorized());
    }

    // ── GET /users/students/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnStudentDetail_When_AdminRequestsStudent() throws Exception {
        when(studentService.findStudentById(1003L)).thenReturn(
            new StudentDetail(1003L, "Ava", "Johnson", "ava.johnson@tcu.edu", true, 1L, "Spring 2026 - Section A", List.of("Pulse Analytics"))
        );

        mockMvc.perform(get("/api/v1/users/students/1003"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.email").value("ava.johnson@tcu.edu"))
            .andExpect(jsonPath("$.data.teamNames[0]").value("Pulse Analytics"));

        verify(studentService).findStudentById(1003L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_StudentDoesNotExist() throws Exception {
        when(studentService.findStudentById(9999L)).thenThrow(new StudentNotFoundException(9999L));

        mockMvc.perform(get("/api/v1/users/students/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No student found with id: 9999"));
    }

    // ── DELETE /users/students/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_DeleteStudent_When_AdminRequestsDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/users/students/1003"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Student deleted successfully."))
            .andExpect(jsonPath("$.data").isEmpty());

        verify(studentService).deleteStudent(1003L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnNotFound_When_DeletedStudentDoesNotExist() throws Exception {
        doThrow(new StudentNotFoundException(9999L))
            .when(studentService).deleteStudent(9999L);

        mockMvc.perform(delete("/api/v1/users/students/9999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No student found with id: 9999"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsStudentDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/users/students/1003"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void should_ReturnForbidden_When_StudentRequestsStudentDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/users/students/1003"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedRequestDeletesStudent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/students/1003"))
            .andExpect(status().isUnauthorized());
    }
}

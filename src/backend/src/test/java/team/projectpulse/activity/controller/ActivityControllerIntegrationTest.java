package team.projectpulse.activity.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.activity.domain.ActivityCategory;
import team.projectpulse.activity.domain.ActivityNotFoundException;
import team.projectpulse.activity.domain.ActivityStatus;
import team.projectpulse.activity.domain.InvalidActivityException;
import team.projectpulse.activity.dto.ActivityDetail;
import team.projectpulse.activity.dto.ActivityWeekOption;
import team.projectpulse.activity.dto.ActivityWorkspace;
import team.projectpulse.activity.service.ActivityService;
import team.projectpulse.config.ControllerTestSecurityConfig;
import team.projectpulse.config.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ActivityController.class)
@Import({ActivityExceptionHandler.class, SecurityConfig.class, ControllerTestSecurityConfig.class})
@ActiveProfiles("test")
class ActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_LoadWorkspace_When_StudentRequestsIt() throws Exception {
        when(activityService.loadWorkspace("ava@tcu.edu", "2026-W16")).thenReturn(buildWorkspace());

        mockMvc.perform(get("/api/v1/activities/workspace").param("week", "2026-W16"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.sectionName").value("Spring 2026 - Section A"))
            .andExpect(jsonPath("$.data.teamName").value("Pulse Analytics"))
            .andExpect(jsonPath("$.data.activities[0].plannedActivity").value("Implement endpoint"));

        verify(activityService).loadWorkspace("ava@tcu.edu", "2026-W16");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnForbidden_When_AdminLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/activities/workspace"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/activities/workspace"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/activities/workspace"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_CreateActivity_When_StudentSubmitsValidPayload() throws Exception {
        when(activityService.createActivity(any(), any())).thenReturn(buildActivityDetail());

        mockMvc.perform(post("/api/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "week": "2026-W16",
                      "category": "DEVELOPMENT",
                      "plannedActivity": "Implement endpoint",
                      "description": "Built the WAR workspace endpoint",
                      "plannedHours": 4.5,
                      "actualHours": 5.0,
                      "status": "DONE"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("WAR has been updated."))
            .andExpect(jsonPath("$.data.activityId").value(1));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_UpdateActivity_When_StudentConfirmsChanges() throws Exception {
        when(activityService.updateActivity(any(), any(), any())).thenReturn(buildActivityDetail());

        mockMvc.perform(put("/api/v1/activities/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "category": "TESTING",
                      "plannedActivity": "Validate forms",
                      "description": "Updated frontend validation rules",
                      "plannedHours": 2.0,
                      "actualHours": 2.5,
                      "status": "UNDER_TESTING"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("WAR has been updated."));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_DeleteActivity_When_StudentConfirmsDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/activities/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("WAR has been updated."));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_ReturnBadRequest_When_ActivityPayloadIsInvalid() throws Exception {
        when(activityService.createActivity(any(), any()))
            .thenThrow(new InvalidActivityException("Future weeks cannot be selected."));

        mockMvc.perform(post("/api/v1/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "week": "2026-W99",
                      "category": "DEVELOPMENT",
                      "plannedActivity": "Implement endpoint",
                      "description": "Built the WAR workspace endpoint",
                      "plannedHours": 4.5,
                      "actualHours": 5.0,
                      "status": "DONE"
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Future weeks cannot be selected."));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_ReturnNotFound_When_ActivityDoesNotExist() throws Exception {
        when(activityService.updateActivity(any(), any(), any()))
            .thenThrow(new ActivityNotFoundException(99L));

        mockMvc.perform(put("/api/v1/activities/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "category": "TESTING",
                      "plannedActivity": "Validate forms",
                      "description": "Updated frontend validation rules",
                      "plannedHours": 2.0,
                      "actualHours": 2.5,
                      "status": "UNDER_TESTING"
                    }
                    """))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No activity found with id: 99"));
    }

    private ActivityWorkspace buildWorkspace() {
        return new ActivityWorkspace(
            1L,
            "Spring 2026 - Section A",
            2001L,
            "Pulse Analytics",
            "2026-W16",
            List.of(new ActivityWeekOption("2026-W16", "Week 16 (2026)", true)),
            List.of(buildActivityDetail()),
            true,
            null
        );
    }

    private ActivityDetail buildActivityDetail() {
        return new ActivityDetail(
            1L,
            "2026-W16",
            ActivityCategory.DEVELOPMENT,
            "Implement endpoint",
            "Built the WAR workspace endpoint",
            new BigDecimal("4.50"),
            new BigDecimal("5.00"),
            ActivityStatus.DONE
        );
    }
}

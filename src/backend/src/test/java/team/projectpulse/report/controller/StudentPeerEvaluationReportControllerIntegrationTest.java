package team.projectpulse.report.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.config.ControllerTestSecurityConfig;
import team.projectpulse.report.domain.InvalidPeerEvaluationReportException;
import team.projectpulse.report.dto.ReportWeekOption;
import team.projectpulse.report.dto.StudentPeerEvaluationCriterionAverageDto;
import team.projectpulse.report.dto.StudentPeerEvaluationReportResponse;
import team.projectpulse.report.service.ReportService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentPeerEvaluationReportController.class)
@Import({ReportExceptionHandler.class, ControllerTestSecurityConfig.class})
@ActiveProfiles("test")
class StudentPeerEvaluationReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    @WithMockUser(username = "ava.johnson@tcu.edu", roles = "STUDENT")
    void should_LoadReportWithDefaultWeek_When_StudentRequestsIt() throws Exception {
        when(reportService.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", null)).thenReturn(response());

        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.studentName").value("Ava Johnson"))
            .andExpect(jsonPath("$.data.selectedWeek").value("2026-W16"));
    }

    @Test
    @WithMockUser(username = "ava.johnson@tcu.edu", roles = "STUDENT")
    void should_LoadReportForExplicitValidWeek_When_StudentRequestsIt() throws Exception {
        when(reportService.generateOwnPeerEvaluationReport("ava.johnson@tcu.edu", "2026-W16")).thenReturn(response());

        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me").param("week", "2026-W16"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.selectedWeek").value("2026-W16"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnForbidden_When_AdminRequestsReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorRequestsReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserRequestsReport() throws Exception {
        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ava.johnson@tcu.edu", roles = "STUDENT")
    void should_ReturnBadRequest_When_WeekIsInvalid() throws Exception {
        when(reportService.generateOwnPeerEvaluationReport(eq("ava.johnson@tcu.edu"), eq("2026-W18")))
            .thenThrow(new InvalidPeerEvaluationReportException("The selected week is not available for your peer evaluation report."));

        mockMvc.perform(get("/api/v1/reports/peer-evaluations/me").param("week", "2026-W18"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("The selected week is not available for your peer evaluation report."));
    }

    private StudentPeerEvaluationReportResponse response() {
        return new StudentPeerEvaluationReportResponse(
            1L,
            "Spring 2026 - Section A",
            2001L,
            "Pulse Analytics",
            1003L,
            "Ava Johnson",
            "2026-W16",
            "Week 16 (2026)",
            "04-13-2026 -- 04-19-2026",
            List.of(new ReportWeekOption("2026-W16", "Week 16 (2026)", "04-13-2026 -- 04-19-2026")),
            true,
            null,
            List.of(new StudentPeerEvaluationCriterionAverageDto(1L, "Participation", "Participates", new BigDecimal("8.50"), 10)),
            List.of("Great teammate."),
            new BigDecimal("17.00"),
            20
        );
    }

}

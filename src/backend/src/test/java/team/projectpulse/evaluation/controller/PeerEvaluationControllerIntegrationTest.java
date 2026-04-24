package team.projectpulse.evaluation.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;
import team.projectpulse.config.JwtAuthFilter;
import team.projectpulse.config.JwtUtils;
import team.projectpulse.config.SecurityConfig;
import team.projectpulse.evaluation.domain.InvalidPeerEvaluationException;
import team.projectpulse.evaluation.domain.PeerEvaluationSubmissionNotFoundException;
import team.projectpulse.evaluation.dto.EvaluationCriterionDto;
import team.projectpulse.evaluation.dto.EvaluationCriterionScoreDraftDto;
import team.projectpulse.evaluation.dto.EvaluationMemberDraftDto;
import team.projectpulse.evaluation.dto.EvaluationWorkspace;
import team.projectpulse.evaluation.dto.PeerEvaluationSubmissionDetail;
import team.projectpulse.evaluation.service.EvaluationService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PeerEvaluationController.class)
@Import({PeerEvaluationExceptionHandler.class, SecurityConfig.class, PeerEvaluationControllerIntegrationTest.TestConfig.class})
@ActiveProfiles("test")
class PeerEvaluationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StubEvaluationService evaluationService;

    @BeforeEach
    void resetServiceState() {
        evaluationService.workspace = null;
        evaluationService.detail = null;
        evaluationService.invalidMessage = null;
        evaluationService.notFoundSubmissionId = null;
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_LoadWorkspace_When_StudentRequestsIt() throws Exception {
        evaluationService.workspace = buildWorkspace();

        mockMvc.perform(get("/api/v1/peer-evaluations/workspace"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.data.sectionName").value("Spring 2026 - Section A"))
            .andExpect(jsonPath("$.data.teamName").value("Pulse Analytics"))
            .andExpect(jsonPath("$.data.criteria[0].criterion").value("Participation"))
            .andExpect(jsonPath("$.data.members[0].fullName").value("Ava Johnson"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void should_ReturnForbidden_When_AdminLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/peer-evaluations/workspace"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/peer-evaluations/workspace"))
            .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserLoadsWorkspace() throws Exception {
        mockMvc.perform(get("/api/v1/peer-evaluations/workspace"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_CreateSubmission_When_StudentSubmitsValidPayload() throws Exception {
        evaluationService.detail = new PeerEvaluationSubmissionDetail(900L, "2026-W16", LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(post("/api/v1/peer-evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Peer evaluation has been submitted."))
            .andExpect(jsonPath("$.data.submissionId").value(900));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_UpdateSubmission_When_StudentConfirmsChanges() throws Exception {
        evaluationService.detail = new PeerEvaluationSubmissionDetail(900L, "2026-W16", LocalDateTime.now(), LocalDateTime.now());

        mockMvc.perform(put("/api/v1/peer-evaluations/900")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.flag").value(true))
            .andExpect(jsonPath("$.message").value("Peer evaluation has been updated."));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_ReturnBadRequest_When_SubmissionPayloadIsInvalid() throws Exception {
        evaluationService.invalidMessage = "Scores must be integers.";

        mockMvc.perform(post("/api/v1/peer-evaluations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("Scores must be integers."));
    }

    @Test
    @WithMockUser(username = "ava@tcu.edu", roles = "STUDENT")
    void should_ReturnNotFound_When_SubmissionDoesNotExist() throws Exception {
        evaluationService.notFoundSubmissionId = 999L;

        mockMvc.perform(put("/api/v1/peer-evaluations/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.flag").value(false))
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("No peer evaluation submission found with id: 999"));
    }

    private EvaluationWorkspace buildWorkspace() {
        return new EvaluationWorkspace(
            1L,
            "Spring 2026 - Section A",
            2001L,
            "Pulse Analytics",
            "2026-W16",
            "Week 16 (2026)",
            "04-13-2026 -- 04-19-2026",
            900L,
            true,
            true,
            true,
            null,
            "2026-04-24T23:59",
            List.of(
                new EvaluationCriterionDto(501L, "Participation", "Participates", 10.0),
                new EvaluationCriterionDto(502L, "Communication", "Communicates", 10.0)
            ),
            List.of(
                new EvaluationMemberDraftDto(
                    100L,
                    "Ava Johnson",
                    true,
                    "Self comment",
                    "Private self comment",
                    List.of(
                        new EvaluationCriterionScoreDraftDto(501L, 9),
                        new EvaluationCriterionScoreDraftDto(502L, 10)
                    )
                )
            )
        );
    }

    private String validPayload() {
        return """
            {
              "evaluations": [
                {
                  "evaluateeStudentId": 100,
                  "publicComment": "Self comment",
                  "privateComment": "Private self comment",
                  "criterionScores": [
                    { "criterionId": 501, "score": 9 },
                    { "criterionId": 502, "score": 10 }
                  ]
                }
              ]
            }
            """;
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        StubEvaluationService evaluationService() {
            return new StubEvaluationService();
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.withUsername(username)
                .password("{noop}password")
                .roles("STUDENT")
                .build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        JwtUtils jwtUtils() {
            return new JwtUtils("test-secret-key-for-testing-only-32chars!!");
        }

        @Bean
        JwtAuthFilter jwtAuthFilter(JwtUtils jwtUtils) {
            return new JwtAuthFilter(jwtUtils) {
                @Override
                protected void doFilterInternal(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain filterChain
                ) throws ServletException, IOException {
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    static class StubEvaluationService extends EvaluationService {

        private EvaluationWorkspace workspace;
        private PeerEvaluationSubmissionDetail detail;
        private String invalidMessage;
        private Long notFoundSubmissionId;

        StubEvaluationService() {
            super(null, null, null, null);
        }

        @Override
        public EvaluationWorkspace loadWorkspace(String studentEmail) {
            return workspace;
        }

        @Override
        public PeerEvaluationSubmissionDetail createSubmission(
            String studentEmail,
            team.projectpulse.evaluation.dto.PeerEvaluationSubmissionRequest request
        ) {
            if (invalidMessage != null) {
                throw new InvalidPeerEvaluationException(invalidMessage);
            }
            return detail;
        }

        @Override
        public PeerEvaluationSubmissionDetail updateSubmission(
            String studentEmail,
            Long submissionId,
            team.projectpulse.evaluation.dto.PeerEvaluationSubmissionRequest request
        ) {
            if (notFoundSubmissionId != null) {
                throw new PeerEvaluationSubmissionNotFoundException(notFoundSubmissionId);
            }
            if (invalidMessage != null) {
                throw new InvalidPeerEvaluationException(invalidMessage);
            }
            return detail;
        }
    }
}

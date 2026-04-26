package team.projectpulse.invite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team.projectpulse.config.ControllerTestSecurityConfig;
import team.projectpulse.config.SecurityConfig;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.invite.dto.InviteSendResult;
import team.projectpulse.invite.service.InstructorInviteService;
import team.projectpulse.user.domain.User;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InstructorInviteController.class)
@Import({InviteExceptionHandler.class, SecurityConfig.class, ControllerTestSecurityConfig.class})
@ActiveProfiles("test")
class InstructorInviteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstructorInviteService instructorInviteService;

    // ── Preview ──────────────────────────────────────────────────────────────

    @Test
    void should_ReturnPreview_When_AdminSendsValidEmails() throws Exception {
        InvitePreview preview = new InvitePreview(
                List.of("ivy@tcu.edu", "noah@tcu.edu"),
                2,
                InstructorInviteService.DEFAULT_SUBJECT,
                "Hello, Admin has invited you..."
        );
        when(instructorInviteService.preview(eq("ivy@tcu.edu; noah@tcu.edu"), any(User.class)))
                .thenReturn(preview);

        mockMvc.perform(post("/api/v1/instructors/invites/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("emailsInput", "ivy@tcu.edu; noah@tcu.edu")))
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.emailCount").value(2))
                .andExpect(jsonPath("$.data.emails[0]").value("ivy@tcu.edu"));

        verify(instructorInviteService).preview(eq("ivy@tcu.edu; noah@tcu.edu"), any(User.class));
    }

    @Test
    void should_ReturnBadRequest_When_EmailsInputIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/instructors/invites/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("emailsInput", "")))
                        .with(authentication(adminAuth())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void should_ReturnBadRequest_When_EmailFormatIsInvalid() throws Exception {
        when(instructorInviteService.preview(eq("bad-email"), any(User.class)))
                .thenThrow(new InvalidEmailFormatException(List.of("bad-email")));

        mockMvc.perform(post("/api/v1/instructors/invites/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("emailsInput", "bad-email")))
                        .with(authentication(adminAuth())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.data[0]").value("bad-email"));
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorAttemptsPreview() throws Exception {
        mockMvc.perform(post("/api/v1/instructors/invites/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("emailsInput", "ivy@tcu.edu"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserAttemptsPreview() throws Exception {
        mockMvc.perform(post("/api/v1/instructors/invites/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("emailsInput", "ivy@tcu.edu"))))
                .andExpect(status().isUnauthorized());
    }

    // ── Send ─────────────────────────────────────────────────────────────────

    @Test
    void should_ReturnSentCount_When_AdminSendsInvitations() throws Exception {
        when(instructorInviteService.send(
                eq(List.of("ivy@tcu.edu")),
                eq(InstructorInviteService.DEFAULT_SUBJECT),
                any(String.class),
                any(User.class)
        )).thenReturn(new InviteSendResult(1));

        Map<String, Object> body = Map.of(
                "emails", List.of("ivy@tcu.edu"),
                "subject", InstructorInviteService.DEFAULT_SUBJECT,
                "body", "Hello, please register."
        );

        mockMvc.perform(post("/api/v1/instructors/invites/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(authentication(adminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.message").value("Invitations sent successfully."))
                .andExpect(jsonPath("$.data.sentCount").value(1));
    }

    @Test
    void should_ReturnBadRequest_When_SendRequestHasEmptyEmails() throws Exception {
        Map<String, Object> body = Map.of(
                "emails", List.of(),
                "subject", "Subject",
                "body", "Body"
        );

        mockMvc.perform(post("/api/v1/instructors/invites/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .with(authentication(adminAuth())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "INSTRUCTOR")
    void should_ReturnForbidden_When_InstructorAttemptsSend() throws Exception {
        Map<String, Object> body = Map.of(
                "emails", List.of("ivy@tcu.edu"),
                "subject", "Subject",
                "body", "Body"
        );

        mockMvc.perform(post("/api/v1/instructors/invites/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden());
    }

    @Test
    void should_ReturnUnauthorized_When_UnauthenticatedUserAttemptsSend() throws Exception {
        Map<String, Object> body = Map.of(
                "emails", List.of("ivy@tcu.edu"),
                "subject", "Subject",
                "body", "Body"
        );

        mockMvc.perform(post("/api/v1/instructors/invites/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Authentication adminAuth() {
        User admin = new User();
        admin.setId(1L);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@tcu.edu");
        admin.setPassword("encoded");
        admin.setRoles("admin");
        admin.setEnabled(true);
        return new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
    }
}

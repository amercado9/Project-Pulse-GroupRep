package team.projectpulse.invite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.invite.dto.InviteSendResult;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorInviteServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InstructorInviteService service;

    private User admin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        admin = buildAdmin();
        when(userRepository.findByEmail("admin@tcu.edu")).thenReturn(Optional.of(admin));
    }

    // ── Preview ──────────────────────────────────────────────────────────────

    @Test
    void should_ReturnPreviewWithDefaultSubjectAndBody_When_EmailsAreValid() {
        InvitePreview preview = service.preview("ivy@tcu.edu; noah@tcu.edu", "admin@tcu.edu");

        assertEquals(2, preview.emailCount());
        assertEquals(List.of("ivy@tcu.edu", "noah@tcu.edu"), preview.emails());
        assertEquals(InstructorInviteService.DEFAULT_SUBJECT, preview.subject());
        assertTrue(preview.body().contains("Admin User"));
        assertTrue(preview.body().contains("admin@tcu.edu"));
        assertTrue(preview.body().contains("[Registration link]"));
    }

    @Test
    void should_Deduplicate_When_SameEmailAppearsMoreThanOnce() {
        InvitePreview preview = service.preview("ivy@tcu.edu; ivy@tcu.edu", "admin@tcu.edu");

        assertEquals(1, preview.emailCount());
        assertEquals(List.of("ivy@tcu.edu"), preview.emails());
    }

    @Test
    void should_ThrowInvalidEmailFormat_When_NoEmailsProvided() {
        assertThrows(InvalidEmailFormatException.class,
                () -> service.preview("   ;   ; ", "admin@tcu.edu"));
    }

    @Test
    void should_ThrowInvalidEmailFormatWithBadEmails_When_FormatIsWrong() {
        InvalidEmailFormatException ex = assertThrows(InvalidEmailFormatException.class,
                () -> service.preview("not-an-email; ivy@tcu.edu", "admin@tcu.edu"));

        assertEquals(List.of("not-an-email"), ex.getInvalidEmails());
    }

    // ── Send ─────────────────────────────────────────────────────────────────

    @Test
    void should_SendEmailToEachRecipient_When_Inviting() {
        InviteSendResult result = service.send(
                List.of("ivy@tcu.edu", "noah@tcu.edu"),
                InstructorInviteService.DEFAULT_SUBJECT,
                InstructorInviteService.buildDefaultBody("Admin User", "admin@tcu.edu", "[Registration link]"),
                "admin@tcu.edu"
        );

        assertEquals(2, result.sentCount());
        verify(emailService, times(2)).send(anyString(), eq(InstructorInviteService.DEFAULT_SUBJECT), anyString());
    }

    @Test
    void should_ReplacePlaceholderWithActualLink_When_Sending() {
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.send(
                List.of("ivy@tcu.edu"),
                InstructorInviteService.DEFAULT_SUBJECT,
                InstructorInviteService.buildDefaultBody("Admin User", "admin@tcu.edu", "[Registration link]"),
                "admin@tcu.edu"
        );

        verify(emailService).send(eq("ivy@tcu.edu"), anyString(), bodyCaptor.capture());

        String sentBody = bodyCaptor.getValue();
        assertTrue(sentBody.contains("http://localhost:5173/register?email="));
        assertTrue(sentBody.contains("ivy%40tcu.edu"));
    }

    @Test
    void should_SendWithCustomBody_When_AdminPersonalizesMessage() {
        String customBody = "Hello! Please register using this link: [Registration link]";
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.send(List.of("ivy@tcu.edu"), InstructorInviteService.DEFAULT_SUBJECT, customBody, "admin@tcu.edu");

        verify(emailService).send(eq("ivy@tcu.edu"), anyString(), bodyCaptor.capture());

        String sentBody = bodyCaptor.getValue();
        assertTrue(sentBody.startsWith("Hello! Please register using this link: http://localhost:5173"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User buildAdmin() {
        User admin = new User();
        admin.setId(1L);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@tcu.edu");
        admin.setPassword("encoded");
        admin.setRoles("admin");
        admin.setEnabled(true);
        return admin;
    }
}

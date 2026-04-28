package team.projectpulse.invite.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.invite.dto.InstructorInviteLinksResult;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstructorInviteServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InstructorInviteService service;

    private User admin;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:5173");
        admin = buildAdmin();
        lenient().when(userRepository.findByEmail("admin@tcu.edu")).thenReturn(Optional.of(admin));
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
    void should_ReturnLinksForEachEmail_When_Sending() {
        InstructorInviteLinksResult result = service.send(List.of("ivy@tcu.edu", "noah@tcu.edu"));

        assertEquals(2, result.links().size());
        assertEquals("ivy@tcu.edu", result.links().get(0).email());
        assertEquals("noah@tcu.edu", result.links().get(1).email());
    }

    @Test
    void should_EncodeEmailInLink_When_Sending() {
        InstructorInviteLinksResult result = service.send(List.of("ivy@tcu.edu"));

        String link = result.links().get(0).link();
        assertTrue(link.startsWith("http://localhost:5173/register?email="));
        assertTrue(link.contains("ivy%40tcu.edu"));
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

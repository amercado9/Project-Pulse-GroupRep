package team.projectpulse.invite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.invite.dto.InviteLink;
import team.projectpulse.invite.dto.InviteLinksResult;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.user.domain.StudentInviteToken;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.StudentInviteTokenRepository;
import team.projectpulse.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class InviteService {

    private static final String EMAIL_SUBJECT =
            "Welcome to The Peer Evaluation Tool - Complete Your Registration";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final StudentInviteTokenRepository tokenRepository;

    @Value("${front-end.url}")
    private String frontendUrl;

    public InviteService(SectionRepository sectionRepository,
                         UserRepository userRepository,
                         StudentInviteTokenRepository tokenRepository) {
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public InvitePreview preview(Long sectionId, String emailsInput, String adminEmail) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new SectionNotFoundException(sectionId));

        User admin = userRepository.findByEmail(adminEmail).orElseThrow();
        List<String> emails = parseEmails(emailsInput);
        validateEmails(emails);

        String body = buildEmailBody(
                admin.getFirstName() + " " + admin.getLastName(),
                admin.getEmail(),
                "[Registration link]"
        );

        return new InvitePreview(emails, emails.size(), EMAIL_SUBJECT, body);
    }

    @Transactional
    public InviteLinksResult send(Long sectionId, List<String> emails) {
        sectionRepository.findById(sectionId)
                .orElseThrow(() -> new SectionNotFoundException(sectionId));

        List<InviteLink> links = emails.stream()
                .map(email -> {
                    String tokenValue = UUID.randomUUID().toString();
                    StudentInviteToken token = new StudentInviteToken();
                    token.setToken(tokenValue);
                    token.setEmail(email.toLowerCase().trim());
                    token.setSectionId(sectionId);
                    token.setExpiresAt(LocalDateTime.now().plusDays(30));
                    tokenRepository.save(token);
                    return new InviteLink(email, frontendUrl + "/join?token=" + tokenValue);
                })
                .toList();

        return new InviteLinksResult(links);
    }

    private List<String> parseEmails(String input) {
        return Arrays.stream(input.split(";"))
                .map(String::trim)
                .filter(e -> !e.isBlank())
                .distinct()
                .toList();
    }

    private void validateEmails(List<String> emails) {
        if (emails.isEmpty()) {
            throw new InvalidEmailFormatException(List.of());
        }
        List<String> invalid = emails.stream()
                .filter(e -> !EMAIL_PATTERN.matcher(e).matches())
                .toList();
        if (!invalid.isEmpty()) {
            throw new InvalidEmailFormatException(invalid);
        }
    }

    private String buildEmailBody(String adminName, String adminEmail, String registrationLink) {
        return """
                Hello,

                %s has invited you to join The Peer Evaluation Tool. To complete your registration, please use the link below:
                %s

                If you have any questions or need assistance, feel free to contact %s or our team directly.

                Please note: This email is not monitored, so do not reply directly to this message.

                Best regards,
                Peer Evaluation Tool Team
                """.formatted(adminName, registrationLink, adminEmail);
    }
}

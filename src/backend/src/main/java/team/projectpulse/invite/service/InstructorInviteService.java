package team.projectpulse.invite.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.invite.dto.InvitePreview;
import team.projectpulse.invite.dto.InviteSendResult;
import team.projectpulse.user.domain.User;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class InstructorInviteService {

    public static final String DEFAULT_SUBJECT =
            "Welcome to The Peer Evaluation Tool - Instructor Account Registration";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
    );

    private final EmailService emailService;

    @Value("${front-end.url}")
    private String frontendUrl;

    public InstructorInviteService(EmailService emailService) {
        this.emailService = emailService;
    }

    public InvitePreview preview(String emailsInput, User admin) {
        List<String> emails = parseEmails(emailsInput);
        validateEmails(emails);

        String adminName = admin.getFirstName() + " " + admin.getLastName();
        String body = buildDefaultBody(adminName, admin.getEmail(), "[Registration link]");

        return new InvitePreview(emails, emails.size(), DEFAULT_SUBJECT, body);
    }

    public InviteSendResult send(List<String> emails, String subject, String body, User admin) {
        for (String email : emails) {
            String registrationLink = frontendUrl + "/register?email="
                    + URLEncoder.encode(email, StandardCharsets.UTF_8);
            String resolvedBody = body.replace("[Registration link]", registrationLink);
            emailService.send(email, subject, resolvedBody);
        }
        return new InviteSendResult(emails.size());
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

    public static String buildDefaultBody(String adminName, String adminEmail, String registrationLink) {
        return """
                Hello,

                %s has invited you to register as an instructor on The Peer Evaluation Tool. To complete your registration, please use the link below:
                %s

                If you have any questions or need assistance, feel free to contact %s or our team directly.

                Please note: This email is not monitored, so do not reply directly to this message.

                Best regards,
                Peer Evaluation Tool Team
                """.formatted(adminName, registrationLink, adminEmail);
    }
}

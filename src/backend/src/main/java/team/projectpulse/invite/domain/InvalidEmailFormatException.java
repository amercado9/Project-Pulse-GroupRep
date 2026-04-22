package team.projectpulse.invite.domain;

import java.util.List;

public class InvalidEmailFormatException extends RuntimeException {

    private final List<String> invalidEmails;

    public InvalidEmailFormatException(List<String> invalidEmails) {
        super(invalidEmails.isEmpty()
                ? "No valid email addresses provided."
                : "Invalid email address(es): " + String.join(", ", invalidEmails));
        this.invalidEmails = invalidEmails;
    }

    public List<String> getInvalidEmails() {
        return invalidEmails;
    }
}

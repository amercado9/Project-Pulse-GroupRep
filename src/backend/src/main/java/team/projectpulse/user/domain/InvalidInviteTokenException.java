package team.projectpulse.user.domain;

public class InvalidInviteTokenException extends RuntimeException {
    public InvalidInviteTokenException() {
        super("This invite link is invalid or has expired.");
    }
}

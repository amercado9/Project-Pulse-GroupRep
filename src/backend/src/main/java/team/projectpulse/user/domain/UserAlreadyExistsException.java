package team.projectpulse.user.domain;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("An account with the email \"" + email + "\" already exists.");
    }
}

package team.projectpulse.user;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("An account with the email \"" + email + "\" already exists.");
    }
}

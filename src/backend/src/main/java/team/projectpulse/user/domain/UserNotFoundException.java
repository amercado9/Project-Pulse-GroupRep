package team.projectpulse.user.domain;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("No student found with id: " + id);
    }
}

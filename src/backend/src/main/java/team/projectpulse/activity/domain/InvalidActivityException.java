package team.projectpulse.activity.domain;

public class InvalidActivityException extends RuntimeException {
    public InvalidActivityException(String message) {
        super(message);
    }
}

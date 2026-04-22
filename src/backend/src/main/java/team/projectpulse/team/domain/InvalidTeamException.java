package team.projectpulse.team.domain;

public class InvalidTeamException extends RuntimeException {
    public InvalidTeamException(String message) {
        super(message);
    }
}

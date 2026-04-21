package team.projectpulse.team.domain;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(Long id) {
        super("No team found with id: " + id);
    }
}

package team.projectpulse.team.domain;

public class TeamAlreadyExistsException extends RuntimeException {
    public TeamAlreadyExistsException(String teamName) {
        super("Team already exists with name: " + teamName);
    }
}

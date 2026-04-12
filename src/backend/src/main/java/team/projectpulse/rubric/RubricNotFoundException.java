package team.projectpulse.rubric;

public class RubricNotFoundException extends RuntimeException {
    public RubricNotFoundException(String type, Long id) {
        super("Could not find " + type + " with id: " + id);
    }
}

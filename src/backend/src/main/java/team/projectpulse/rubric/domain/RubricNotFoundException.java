package team.projectpulse.rubric.domain;

public class RubricNotFoundException extends RuntimeException {
    public RubricNotFoundException(Long id) {
        super("No rubric found with id: " + id);
    }
}

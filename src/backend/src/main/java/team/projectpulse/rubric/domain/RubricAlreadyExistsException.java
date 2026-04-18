package team.projectpulse.rubric.domain;

public class RubricAlreadyExistsException extends RuntimeException {
    public RubricAlreadyExistsException(String name) {
        super("A rubric with the name \"" + name + "\" already exists.");
    }
}

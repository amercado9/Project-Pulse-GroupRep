package team.projectpulse.section.domain;

public class SectionAlreadyExistsException extends RuntimeException {
    public SectionAlreadyExistsException(String name) {
        super("A section with the name \"" + name + "\" already exists.");
    }
}

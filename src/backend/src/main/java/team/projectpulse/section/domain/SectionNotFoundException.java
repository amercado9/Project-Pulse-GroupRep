package team.projectpulse.section.domain;

public class SectionNotFoundException extends RuntimeException {
    public SectionNotFoundException(Long id) {
        super("No section found with id: " + id);
    }
}

package team.projectpulse.team.domain;

public class InstructorNotFoundException extends RuntimeException {

    public InstructorNotFoundException(Long instructorId) {
        super("No instructor found with id: " + instructorId);
    }
}

package team.projectpulse.team.domain;

public class StudentNotFoundException extends RuntimeException {

    public StudentNotFoundException(Long studentId) {
        super("No student found with id: " + studentId);
    }
}

package team.projectpulse.team.domain;

public class StudentNotInTeamException extends RuntimeException {
    public StudentNotInTeamException(Long studentId, Long teamId) {
        super("Student " + studentId + " is not a member of team " + teamId + ".");
    }
}

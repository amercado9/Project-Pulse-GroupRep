package team.projectpulse.evaluation.domain;

public class PeerEvaluationSubmissionNotFoundException extends RuntimeException {

    public PeerEvaluationSubmissionNotFoundException(Long submissionId) {
        super("No peer evaluation submission found with id: " + submissionId);
    }
}

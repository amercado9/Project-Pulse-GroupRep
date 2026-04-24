package team.projectpulse.evaluation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.evaluation.domain.InvalidPeerEvaluationException;
import team.projectpulse.evaluation.domain.PeerEvaluationSubmissionNotFoundException;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestControllerAdvice
public class PeerEvaluationExceptionHandler {

    @ExceptionHandler(PeerEvaluationSubmissionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleSubmissionNotFound(PeerEvaluationSubmissionNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(InvalidPeerEvaluationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidPeerEvaluation(InvalidPeerEvaluationException ex) {
        return Result.error(StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }
}

package team.projectpulse.report.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.report.domain.InvalidPeerEvaluationReportException;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestControllerAdvice
public class ReportExceptionHandler {

    @ExceptionHandler(InvalidPeerEvaluationReportException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidPeerEvaluationReport(InvalidPeerEvaluationReportException ex) {
        return Result.error(StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }
}

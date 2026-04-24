package team.projectpulse.activity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.activity.domain.ActivityNotFoundException;
import team.projectpulse.activity.domain.InvalidActivityException;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestControllerAdvice
public class ActivityExceptionHandler {

    @ExceptionHandler(ActivityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleActivityNotFound(ActivityNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(InvalidActivityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidActivity(InvalidActivityException ex) {
        return Result.error(StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }
}

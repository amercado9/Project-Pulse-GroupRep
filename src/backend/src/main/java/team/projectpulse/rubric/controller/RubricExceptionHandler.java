package team.projectpulse.rubric.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.rubric.domain.RubricAlreadyExistsException;
import team.projectpulse.rubric.domain.RubricNotFoundException;
import team.projectpulse.system.Result;

@RestControllerAdvice
public class RubricExceptionHandler {

    @ExceptionHandler(RubricNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(RubricNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(RubricAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleConflict(RubricAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }
}

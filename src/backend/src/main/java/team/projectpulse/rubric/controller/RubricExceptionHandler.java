package team.projectpulse.rubric.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.rubric.domain.RubricAlreadyExistsException;
import team.projectpulse.rubric.domain.RubricNotFoundException;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

@RestControllerAdvice
public class RubricExceptionHandler {

    @ExceptionHandler(RubricNotFoundException.class)
    public Result<Void> handleNotFound(RubricNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(RubricAlreadyExistsException.class)
    public Result<Void> handleConflict(RubricAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }
}

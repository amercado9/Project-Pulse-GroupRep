package team.projectpulse.section.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.section.domain.SectionAlreadyExistsException;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.system.Result;

@RestControllerAdvice
public class SectionExceptionHandler {

    @ExceptionHandler(SectionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(SectionNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(SectionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleConflict(SectionAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }
}

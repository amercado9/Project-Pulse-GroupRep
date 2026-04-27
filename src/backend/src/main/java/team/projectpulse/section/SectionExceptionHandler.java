package team.projectpulse.section;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.common.Result;

@RestControllerAdvice
public class SectionExceptionHandler {

    @ExceptionHandler(SectionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result<Void> handleNotFound(SectionNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(SectionAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Result<Void> handleAlreadyExists(SectionAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }
}

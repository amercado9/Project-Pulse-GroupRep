package team.projectpulse.rubric;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.common.Result;

@RestControllerAdvice
public class RubricExceptionHandler {

    @ExceptionHandler(RubricNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Result<Void> handleNotFound(RubricNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }
}

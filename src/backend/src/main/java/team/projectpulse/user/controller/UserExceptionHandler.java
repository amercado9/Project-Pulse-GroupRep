package team.projectpulse.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.StudentNotFoundException;
import team.projectpulse.user.domain.UserNotFoundException;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(StudentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleStudentNotFound(StudentNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleUserNotFound(UserNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }
}

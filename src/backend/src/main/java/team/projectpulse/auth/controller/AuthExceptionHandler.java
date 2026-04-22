package team.projectpulse.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.InvalidInviteTokenException;
import team.projectpulse.user.domain.UserAlreadyExistsException;

@RestControllerAdvice
public class AuthExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Result<Void> handleAlreadyExists(UserAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }

    @ExceptionHandler(InvalidInviteTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Result<Void> handleInvalidToken(InvalidInviteTokenException ex) {
        return Result.error(400, ex.getMessage());
    }
}

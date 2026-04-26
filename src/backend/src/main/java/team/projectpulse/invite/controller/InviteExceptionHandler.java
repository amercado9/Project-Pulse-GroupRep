package team.projectpulse.invite.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.invite.domain.InvalidEmailFormatException;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import java.util.List;

@RestControllerAdvice
public class InviteExceptionHandler {

    @ExceptionHandler(InvalidEmailFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<List<String>> handleInvalidEmailFormat(InvalidEmailFormatException ex) {
        return new Result<>(false, StatusCode.INVALID_ARGUMENT, ex.getMessage(), ex.getInvalidEmails());
    }
}

package team.projectpulse.team.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.system.Result;
import team.projectpulse.team.domain.TeamNotFoundException;

@RestControllerAdvice
public class TeamExceptionHandler {

    @ExceptionHandler(TeamNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(TeamNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }
}

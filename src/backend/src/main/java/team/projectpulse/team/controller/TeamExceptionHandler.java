package team.projectpulse.team.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.domain.StudentNotInTeamException;
import team.projectpulse.team.domain.TeamAlreadyExistsException;
import team.projectpulse.team.domain.TeamNotFoundException;

@RestControllerAdvice
public class TeamExceptionHandler {

    @ExceptionHandler(TeamNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(TeamNotFoundException ex) {
        return Result.notFound(ex.getMessage());
    }

    @ExceptionHandler(TeamAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Result<Void> handleConflict(TeamAlreadyExistsException ex) {
        return Result.conflict(ex.getMessage());
    }

    @ExceptionHandler(InvalidTeamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidTeam(InvalidTeamException ex) {
        return Result.error(StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(InvalidTeamStudentAssignmentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidTeamStudentAssignment(InvalidTeamStudentAssignmentException ex) {
        return Result.error(StatusCode.INVALID_ARGUMENT, ex.getMessage());
    }

    @ExceptionHandler(StudentNotInTeamException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleStudentNotInTeam(StudentNotInTeamException ex) {
        return Result.notFound(ex.getMessage());
    }
}

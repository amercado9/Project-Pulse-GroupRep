package team.projectpulse.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.system.Result;
import team.projectpulse.user.dto.StudentDetail;
import team.projectpulse.user.dto.StudentSummary;
import team.projectpulse.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/students")
    public Result<List<StudentSummary>> findStudents(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long   teamId,
            @RequestParam(required = false) String teamName,
            @RequestParam(required = false) Long   sectionId,
            @RequestParam(required = false) String sectionName) {
        return Result.success(userService.findStudents(
                firstName, lastName, email, teamId, teamName, sectionId, sectionName));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    @GetMapping("/students/{id}")
    public Result<StudentDetail> getStudentDetail(@PathVariable Long id) {
        return Result.success(userService.getStudentDetail(id));
    }
}

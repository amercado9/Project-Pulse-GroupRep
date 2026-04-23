package team.projectpulse.user.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
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

    @GetMapping("/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public Result<List<StudentSummary>> findStudents(
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String sectionName,
        @RequestParam(required = false) String teamName
    ) {
        return Result.success(userService.findStudents(firstName, lastName, email, sectionName, teamName));
    }
}

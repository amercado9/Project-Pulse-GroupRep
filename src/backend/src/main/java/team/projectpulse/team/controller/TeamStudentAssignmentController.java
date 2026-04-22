package team.projectpulse.team.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.team.dto.TeamStudentAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamStudentAssignmentsRequest;
import team.projectpulse.team.service.TeamStudentAssignmentService;

@RestController
@RequestMapping("${api.endpoint.base-url}/team-student-assignments")
public class TeamStudentAssignmentController {

    private final TeamStudentAssignmentService teamStudentAssignmentService;

    public TeamStudentAssignmentController(TeamStudentAssignmentService teamStudentAssignmentService) {
        this.teamStudentAssignmentService = teamStudentAssignmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamStudentAssignmentWorkspace> loadWorkspace(@RequestParam Long sectionId) {
        return Result.success(teamStudentAssignmentService.loadWorkspace(sectionId));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamStudentAssignmentWorkspace> updateAssignments(
        @RequestBody UpdateTeamStudentAssignmentsRequest request
    ) {
        return Result.success(
            "Student team assignments saved successfully.",
            teamStudentAssignmentService.updateAssignments(request)
        );
    }
}

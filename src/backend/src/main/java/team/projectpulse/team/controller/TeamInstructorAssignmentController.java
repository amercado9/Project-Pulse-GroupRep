package team.projectpulse.team.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.team.dto.TeamInstructorAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamInstructorAssignmentsRequest;
import team.projectpulse.team.service.TeamInstructorAssignmentService;

@RestController
@RequestMapping("${api.endpoint.base-url}/team-instructor-assignments")
public class TeamInstructorAssignmentController {

    private final TeamInstructorAssignmentService teamInstructorAssignmentService;

    public TeamInstructorAssignmentController(TeamInstructorAssignmentService teamInstructorAssignmentService) {
        this.teamInstructorAssignmentService = teamInstructorAssignmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamInstructorAssignmentWorkspace> loadWorkspace(@RequestParam Long sectionId) {
        return Result.success(teamInstructorAssignmentService.loadWorkspace(sectionId));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamInstructorAssignmentWorkspace> updateAssignments(
        @RequestBody UpdateTeamInstructorAssignmentsRequest request
    ) {
        return Result.success(
            "Instructor team assignments saved successfully.",
            teamInstructorAssignmentService.updateAssignments(request)
        );
    }
}

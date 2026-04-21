package team.projectpulse.team.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.service.TeamService;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public Result<List<TeamSummary>> findTeams(
        @RequestParam(required = false) Long sectionId,
        @RequestParam(required = false) String sectionName,
        @RequestParam(required = false) String teamName,
        @RequestParam(required = false) String instructor
    ) {
        return Result.success(teamService.findTeams(sectionId, sectionName, teamName, instructor));
    }
}

package team.projectpulse.team.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.system.Result;
import team.projectpulse.team.dto.CreateTeamRequest;
import team.projectpulse.team.dto.TeamDetail;
import team.projectpulse.team.dto.TeamSummary;
import team.projectpulse.team.dto.UpdateTeamRequest;
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INSTRUCTOR')")
    public Result<TeamDetail> getTeam(@PathVariable Long id) {
        return Result.success(teamService.findTeamDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDetail> createTeam(@RequestBody CreateTeamRequest request) {
        return Result.success("Team created successfully.", teamService.createTeam(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<TeamDetail> updateTeam(@PathVariable Long id, @RequestBody UpdateTeamRequest request) {
        return Result.success("Team updated successfully.", teamService.updateTeam(id, request));
    }
}

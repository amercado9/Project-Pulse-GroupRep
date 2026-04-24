package team.projectpulse.activity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.activity.dto.ActivityDetail;
import team.projectpulse.activity.dto.ActivityWorkspace;
import team.projectpulse.activity.dto.CreateActivityRequest;
import team.projectpulse.activity.dto.UpdateActivityRequest;
import team.projectpulse.activity.service.ActivityService;
import team.projectpulse.system.Result;

@RestController
@RequestMapping("${api.endpoint.base-url}/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/workspace")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ActivityWorkspace> getWorkspace(
        Authentication authentication,
        @RequestParam(required = false) String week
    ) {
        return Result.success(activityService.loadWorkspace(authentication.getName(), week));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ActivityDetail> createActivity(
        Authentication authentication,
        @RequestBody CreateActivityRequest request
    ) {
        return Result.success("WAR has been updated.", activityService.createActivity(authentication.getName(), request));
    }

    @PutMapping("/{activityId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ActivityDetail> updateActivity(
        Authentication authentication,
        @PathVariable Long activityId,
        @RequestBody UpdateActivityRequest request
    ) {
        return Result.success("WAR has been updated.", activityService.updateActivity(authentication.getName(), activityId, request));
    }

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> deleteActivity(Authentication authentication, @PathVariable Long activityId) {
        activityService.deleteActivity(authentication.getName(), activityId);
        return Result.success("WAR has been updated.", null);
    }
}

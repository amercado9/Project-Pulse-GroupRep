package team.projectpulse.team;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Senior Design Team management endpoints.
 *
 * Use Cases:
 *   UC-7:  Admin/Instructor finds senior design teams
 *   UC-8:  Admin/Instructor views a senior design team
 *   UC-9:  Admin creates a senior design team
 *   UC-10: Admin edits a senior design team
 *   UC-12: Admin assigns students to senior design teams
 *   UC-13: Admin removes a student from a senior design team
 *   UC-14: Admin deletes a senior design team
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/teams")
public class TeamController {
    // TODO: Implement UC-7 to UC-14
}

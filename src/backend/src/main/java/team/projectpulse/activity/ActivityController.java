package team.projectpulse.activity;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Weekly Activity Report (WAR) endpoints.
 *
 * Use Cases:
 *   UC-27: Student manages activities in a Weekly Activity Report
 *   UC-32: Instructor/Student generates a WAR report of a senior design team
 *   UC-34: Instructor generates a WAR report of the student
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/activities")
public class ActivityController {
    // TODO: Implement UC-27, UC-32, UC-34
}

package team.projectpulse.instructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Instructor management endpoints.
 *
 * Use Cases:
 *   UC-21: Admin finds instructors
 *   UC-22: Admin views an instructor
 *   UC-23: Admin deactivates an instructor
 *   UC-24: Admin reactivates an instructor
 *   UC-30: Instructor sets up an instructor account
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/instructors")
public class InstructorController {
    // TODO: Implement UC-21, UC-22, UC-23, UC-24, UC-30
}

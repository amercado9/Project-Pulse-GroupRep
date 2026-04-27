package team.projectpulse.course;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Course management endpoints.
 * Courses are containers for Sections.
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/courses")
public class CourseController {
    // TODO: Implement course CRUD
}

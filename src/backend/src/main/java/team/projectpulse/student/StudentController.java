package team.projectpulse.student;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Student management endpoints.
 *
 * Use Cases:
 *   UC-15: Admin/Instructor finds students
 *   UC-16: Admin/Instructor views a student
 *   UC-17: Admin deletes a student
 *   UC-25: Student sets up a student account
 *   UC-26: Student edits an account
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/students")
public class StudentController {
    // TODO: Implement UC-15, UC-16, UC-17, UC-25, UC-26
}

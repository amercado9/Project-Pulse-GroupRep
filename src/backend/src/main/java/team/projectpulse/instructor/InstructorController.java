package team.projectpulse.instructor;

import org.springframework.web.bind.annotation.*;
import team.projectpulse.common.Result;

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

    private final InstructorService instructorService;

    public InstructorController(InstructorService instructorService) {
        this.instructorService = instructorService;
    }

    /**
     * UC-22: View the details of an instructor.
     * The response includes supervised teams organized by section names.
     */
    @GetMapping("/{instructorId}")
    public Result findInstructorById(@PathVariable Long instructorId) {
        InstructorDTO instructorDTO = instructorService.getInstructorDetails(instructorId);
        return new Result(true, 200, "Find Instructor Success", instructorDTO);
    }
}

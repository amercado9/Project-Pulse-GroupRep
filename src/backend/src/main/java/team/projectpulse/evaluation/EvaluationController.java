package team.projectpulse.evaluation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Peer Evaluation endpoints.
 *
 * Use Cases:
 *   UC-28: Student submits a peer evaluation for the previous week
 *   UC-29: Student views her own peer evaluation report
 *   UC-31: Instructor generates a peer evaluation report of the entire section
 *   UC-33: Instructor generates a peer evaluation report of a student
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/evaluations")
public class EvaluationController {
    // TODO: Implement UC-28, UC-29, UC-31, UC-33
}

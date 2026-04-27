package team.projectpulse.ram.usecase;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Use Case authoring endpoints (RAM module).
 * Use cases are owned by a team and support collaborative editing with locking.
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/teams/{teamId}/use-cases")
public class UseCaseController {
    // TODO: Implement use case CRUD and locking
}

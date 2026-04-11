package team.projectpulse.section;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles Senior Design Section management endpoints.
 *
 * Use Cases:
 *   UC-2:  Admin finds senior design sections
 *   UC-3:  Admin views a senior design section
 *   UC-4:  Admin creates a senior design section
 *   UC-5:  Admin edits a senior design section
 *   UC-6:  Admin sets up active weeks for a section
 *   UC-11: Admin invites students to join a section
 *   UC-18: Admin invites instructors to register an account
 *   UC-19: Admin assigns instructors to senior design teams
 *   UC-20: Admin removes an instructor from a senior design team
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/sections")
public class SectionController {
    // TODO: Implement UC-2 to UC-6, UC-11, UC-18, UC-19, UC-20
}

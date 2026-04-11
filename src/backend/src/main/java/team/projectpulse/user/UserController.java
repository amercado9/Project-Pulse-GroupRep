package team.projectpulse.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles user authentication and account management endpoints.
 * Supports login, password reset, and registration flows.
 */
@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {
    // TODO: Implement login, register, forget-password, reset-password
}

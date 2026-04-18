package team.projectpulse.user.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.base-url}/users")
public class UserController {
    // User management endpoints (profile, invite, find, delete) go here.
}

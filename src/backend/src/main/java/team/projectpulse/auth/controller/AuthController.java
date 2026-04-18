package team.projectpulse.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import team.projectpulse.auth.service.AuthService;
import team.projectpulse.config.JwtUtils;
import team.projectpulse.system.Result;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.dto.RegisterRequest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("${api.endpoint.base-url}/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils,
                          AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            return Result.error(401, "Missing credentials");
        }

        String decoded = new String(Base64.getDecoder().decode(header.substring(6)), StandardCharsets.UTF_8);
        int colon = decoded.indexOf(':');
        if (colon < 1) {
            return Result.error(401, "Invalid credentials format");
        }
        String username = decoded.substring(0, colon);
        String password = decoded.substring(colon + 1);

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = (User) auth.getPrincipal();
        String token = jwtUtils.generateToken(user);
        return Result.success("Login successful.", Map.of("token", token, "userInfo", user));
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterRequest request) {
        authService.registerStudent(request);
        return Result.success("Account created. Please log in.", null);
    }
}

package team.projectpulse.user.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import team.projectpulse.auth.service.AuthService;
import team.projectpulse.user.dto.RegisterRequest;
import team.projectpulse.user.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthService authService;

    public DataInitializer(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@tcu.edu").isEmpty()) {
            authService.register(
                new RegisterRequest("Admin", "User", "admin@tcu.edu", "Admin1234!"),
                "admin instructor"
            );
        }
    }
}

package team.projectpulse.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@tcu.edu").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@tcu.edu");
            admin.setPassword(passwordEncoder.encode("Admin1234!"));
            admin.setRoles("admin instructor");
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("[DataInitializer] Seeded admin user: admin@tcu.edu");
        }
    }
}

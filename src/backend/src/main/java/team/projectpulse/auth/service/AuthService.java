package team.projectpulse.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.domain.UserAlreadyExistsException;
import team.projectpulse.user.dto.RegisterRequest;
import team.projectpulse.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerStudent(RegisterRequest req) {
        return register(req, "student");
    }

    public User register(RegisterRequest req, String role) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }
        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles(role);
        user.setEnabled(true);
        return userRepository.save(user);
    }
}

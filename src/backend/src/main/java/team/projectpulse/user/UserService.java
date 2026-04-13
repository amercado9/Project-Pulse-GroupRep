package team.projectpulse.user;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No account found for: " + email));
    }

    /**
     * UC-25: Register a new student account.
     * Throws UserAlreadyExistsException if the email is already registered.
     */
    public User registerStudent(RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email().trim())) {
            throw new UserAlreadyExistsException(req.email().trim());
        }
        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(req.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles("student");
        user.setEnabled(true);
        return userRepository.save(user);
    }
}

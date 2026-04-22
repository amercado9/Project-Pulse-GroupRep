package team.projectpulse.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.auth.dto.InviteTokenInfo;
import team.projectpulse.auth.dto.StudentRegisterRequest;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.user.domain.InvalidInviteTokenException;
import team.projectpulse.user.domain.StudentInviteToken;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.domain.UserAlreadyExistsException;
import team.projectpulse.user.dto.RegisterRequest;
import team.projectpulse.user.repository.StudentInviteTokenRepository;
import team.projectpulse.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentInviteTokenRepository tokenRepository;
    private final SectionRepository sectionRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       StudentInviteTokenRepository tokenRepository,
                       SectionRepository sectionRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.sectionRepository = sectionRepository;
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

    public InviteTokenInfo validateStudentInviteToken(String token) {
        StudentInviteToken invite = tokenRepository.findByToken(token)
                .orElseThrow(InvalidInviteTokenException::new);

        if (invite.isUsed() || invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInviteTokenException();
        }

        if (userRepository.existsByEmailIgnoreCase(invite.getEmail())) {
            throw new UserAlreadyExistsException(invite.getEmail());
        }

        String sectionName = sectionRepository.findById(invite.getSectionId())
                .map(Section::getSectionName)
                .orElse(null);

        return new InviteTokenInfo(invite.getEmail(), sectionName);
    }

    @Transactional
    public void registerStudentWithToken(StudentRegisterRequest req) {
        StudentInviteToken invite = tokenRepository.findByToken(req.token())
                .orElseThrow(InvalidInviteTokenException::new);

        if (invite.isUsed() || invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidInviteTokenException();
        }

        String email = invite.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserAlreadyExistsException(email);
        }

        User user = new User();
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRoles("student");
        user.setEnabled(true);
        userRepository.save(user);

        invite.setUsed(true);
        tokenRepository.save(invite);
    }
}

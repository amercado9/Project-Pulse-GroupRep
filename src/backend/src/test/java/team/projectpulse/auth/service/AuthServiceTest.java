package team.projectpulse.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.domain.UserAlreadyExistsException;
import team.projectpulse.user.dto.RegisterRequest;
import team.projectpulse.user.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void should_ThrowUserAlreadyExistsException_When_EmailAlreadyRegistered() {
        RegisterRequest req = new RegisterRequest("Alice", "Test", "alice@tcu.edu", "pass123");
        when(userRepository.existsByEmailIgnoreCase("alice@tcu.edu")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(req, "student"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void should_SaveUser_When_EmailIsNew() {
        RegisterRequest req = new RegisterRequest("Alice", "Test", "alice@tcu.edu", "pass123");
        when(userRepository.existsByEmailIgnoreCase("alice@tcu.edu")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(req, "student");

        assertEquals("alice@tcu.edu", saved.getEmail());
        assertEquals("Alice", saved.getFirstName());
        assertEquals("student", saved.getRoles());
        assertTrue(saved.isEnabled());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void should_LowercaseEmail_When_Registering() {
        RegisterRequest req = new RegisterRequest("Bob", "Smith", "BOB@TCU.EDU", "pass123");
        when(userRepository.existsByEmailIgnoreCase("bob@tcu.edu")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(req, "student");

        assertEquals("bob@tcu.edu", saved.getEmail());
    }

    @Test
    void should_RegisterStudent_When_CallingRegisterStudent() {
        RegisterRequest req = new RegisterRequest("Carol", "Lee", "carol@tcu.edu", "pass123");
        when(userRepository.existsByEmailIgnoreCase("carol@tcu.edu")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.registerStudent(req);

        assertEquals("student", saved.getRoles());
    }
}

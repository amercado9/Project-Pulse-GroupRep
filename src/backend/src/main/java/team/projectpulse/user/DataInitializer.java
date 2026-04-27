package team.projectpulse.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import team.projectpulse.section.Section;
import team.projectpulse.section.SectionRepository;
import team.projectpulse.team.Team;
import team.projectpulse.team.TeamRepository;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SectionRepository sectionRepository,
                           TeamRepository teamRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
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
            admin = userRepository.save(admin);
            System.out.println("[DataInitializer] Seeded admin user: admin@tcu.edu");

            // Seed some data for UC-22 testing
            Section s1 = new Section();
            s1.setSectionName("Senior Design 1");
            s1 = sectionRepository.save(s1);

            Team t1 = new Team();
            t1.setTeamName("Project Pulse");
            t1.setSection(s1);
            t1.setInstructors(Set.of(admin));
            teamRepository.save(t1);

            System.out.println("[DataInitializer] Seeded Section and Team for admin");
        }
    }
}

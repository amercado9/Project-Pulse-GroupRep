package team.projectpulse.user.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import team.projectpulse.section.domain.Section;
import team.projectpulse.user.domain.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void should_ReturnOnlyEnabledStudentsInSectionOrderedByName() {
        Section sectionA = persistSection("Spring 2026 - Section A");
        Section sectionB = persistSection("Spring 2026 - Section B");

        persistUser("Zoe", "Zimmer", "zoe@tcu.edu", "student", true, sectionA);
        persistUser("Amy", "Adams", "amy@tcu.edu", "student", true, sectionA);
        persistUser("Ian", "Stone", "ian@tcu.edu", "student", false, sectionA);
        persistUser("Noah", "Bennett", "noah@tcu.edu", "instructor", true, sectionA);
        persistUser("Mia", "Chen", "mia@tcu.edu", "student", true, sectionB);

        entityManager.flush();
        entityManager.clear();

        List<User> students = userRepository.findEnabledStudentsBySectionId(sectionA.getSectionId());

        assertEquals(List.of("Amy", "Zoe"), students.stream().map(User::getFirstName).toList());
        assertEquals(List.of("amy@tcu.edu", "zoe@tcu.edu"), students.stream().map(User::getEmail).toList());
    }

    private Section persistSection(String sectionName) {
        Section section = new Section();
        section.setSectionName(sectionName);
        section.setActive(true);
        entityManager.persist(section);
        return section;
    }

    private void persistUser(
        String firstName,
        String lastName,
        String email,
        String roles,
        boolean enabled,
        Section section
    ) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles(roles);
        user.setEnabled(enabled);
        user.setSection(section);
        entityManager.persist(user);
    }
}

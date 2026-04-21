package team.projectpulse.team.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityManager;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.user.domain.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
class TeamRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void should_ReturnTeamsSortedBySectionDescThenTeamAsc_When_NoFilters() {
        SeededData data = seedTeams();

        List<Team> teams = teamRepository.search(null, null, null, null);

        assertEquals(List.of(data.teamAlpha, data.teamBravo, data.teamGamma),
            teams.stream().map(Team::getTeamName).toList());
    }

    @Test
    void should_FilterBySectionNameTeamNameAndInstructor_When_FiltersProvided() {
        seedTeams();

        List<Team> teams = teamRepository.search(null, "Spring 2026 - Section A", "pulse", "ivy");

        assertEquals(List.of("Pulse Analytics"), teams.stream().map(Team::getTeamName).toList());
    }

    @Test
    void should_ReturnEmptyList_When_NoTeamsMatchFilters() {
        seedTeams();

        List<Team> teams = teamRepository.search(null, null, "missing", null);

        assertEquals(List.of(), teams);
    }

    private SeededData seedTeams() {
        Section sectionA = new Section();
        sectionA.setSectionName("Spring 2026 - Section A");
        sectionA.setStartDate(LocalDate.of(2026, 1, 13));
        sectionA.setEndDate(LocalDate.of(2026, 5, 10));
        sectionA.setActive(true);
        entityManager.persist(sectionA);

        Section sectionB = new Section();
        sectionB.setSectionName("Fall 2025 - Section B");
        sectionB.setStartDate(LocalDate.of(2025, 8, 25));
        sectionB.setEndDate(LocalDate.of(2025, 12, 15));
        sectionB.setActive(true);
        entityManager.persist(sectionB);

        User ivy = persistUser("Ivy", "Stone", "ivy.repo@tcu.edu", "instructor");
        User noah = persistUser("Noah", "Bennett", "noah.repo@tcu.edu", "instructor");
        User ava = persistUser("Ava", "Johnson", "ava.repo@tcu.edu", "student");
        User liam = persistUser("Liam", "Parker", "liam.repo@tcu.edu", "student");
        User mia = persistUser("Mia", "Chen", "mia.repo@tcu.edu", "student");

        Team pulse = persistTeam(sectionA, "Pulse Analytics", ivy, List.of(ava, liam));
        Team review = persistTeam(sectionA, "Review Board", noah, List.of(mia));
        Team gamma = persistTeam(sectionB, "Gamma Studio", noah, List.of(ava, mia));

        entityManager.flush();
        entityManager.clear();

        return new SeededData("Review Board", "Pulse Analytics", "Gamma Studio");
    }

    private User persistUser(String firstName, String lastName, String email, String roles) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles(roles);
        user.setEnabled(true);
        entityManager.persist(user);
        return user;
    }

    private Team persistTeam(Section section, String teamName, User instructor, List<User> students) {
        Team team = new Team();
        team.setSection(section);
        team.setTeamName(teamName);
        team.setTeamDescription(teamName + " description");
        team.setTeamWebsiteUrl("https://" + teamName.toLowerCase().replace(" ", "-") + ".example.com");
        team.setInstructors(new LinkedHashSet<>(List.of(instructor)));
        team.setStudents(new LinkedHashSet<>(students));
        entityManager.persist(team);
        return team;
    }

    private record SeededData(String teamBravo, String teamAlpha, String teamGamma) {}
}

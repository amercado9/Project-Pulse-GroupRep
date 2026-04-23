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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void should_FetchSectionStudentsAndInstructors_When_FindingTeamDetailById() {
        SeededData data = seedTeams();

        Optional<Team> teamOptional = teamRepository.findDetailById(data.pulseAnalyticsId());

        assertTrue(teamOptional.isPresent());
        Team team = teamOptional.get();
        assertEquals("Pulse Analytics", team.getTeamName());
        assertEquals("Spring 2026 - Section A", team.getSection().getSectionName());
        assertEquals(List.of("Ava", "Liam"), team.getStudents().stream().map(User::getFirstName).sorted().toList());
        assertEquals(List.of("Ivy"), team.getInstructors().stream().map(User::getFirstName).sorted().toList());
    }

    @Test
    void should_ReturnEmpty_When_TeamIdDoesNotExist() {
        seedTeams();

        Optional<Team> team = teamRepository.findDetailById(99999L);

        assertTrue(team.isEmpty());
    }

    @Test
    void should_ReturnTrue_When_TeamNameExistsIgnoringCase() {
        seedTeams();

        boolean exists = teamRepository.existsByTeamNameIgnoreCase("pulse analytics");

        assertTrue(exists);
    }

    @Test
    void should_ReturnFalse_When_TeamNameDoesNotExist() {
        seedTeams();

        boolean exists = teamRepository.existsByTeamNameIgnoreCase("Missing Team");

        assertEquals(false, exists);
    }

    @Test
    void should_ReturnTrue_When_AnotherTeamHasSameNameIgnoringCase() {
        SeededData data = seedTeams();

        boolean exists = teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("pulse analytics", data.reviewBoardId());

        assertTrue(exists);
    }

    @Test
    void should_ReturnFalse_When_OnlySameTeamHasThatName() {
        SeededData data = seedTeams();

        boolean exists = teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("pulse analytics", data.pulseAnalyticsId());

        assertEquals(false, exists);
    }

    @Test
    void should_ReturnFalse_When_NameDoesNotExistForDifferentTeam() {
        SeededData data = seedTeams();

        boolean exists = teamRepository.existsByTeamNameIgnoreCaseAndTeamIdNot("Missing Team", data.pulseAnalyticsId());

        assertEquals(false, exists);
    }

    @Test
    void should_FetchAllTeamsBySectionWithStudentsOrderedByTeamName() {
        SeededData data = seedTeams();

        List<Team> teams = teamRepository.findAllBySectionIdWithStudentsOrdered(data.sectionAId());

        assertEquals(List.of("Pulse Analytics", "Review Board"), teams.stream().map(Team::getTeamName).toList());
        assertEquals(List.of("Ava", "Liam"), teams.getFirst().getStudents().stream().map(User::getFirstName).sorted().toList());
        assertEquals(List.of("Mia"), teams.get(1).getStudents().stream().map(User::getFirstName).toList());
    }

    @Test
    void should_FetchAllTeamsBySectionWithInstructorsOrderedByTeamName() {
        SeededData data = seedTeams();

        List<Team> teams = teamRepository.findAllBySectionIdWithInstructorsOrdered(data.sectionAId());

        assertEquals(List.of("Pulse Analytics", "Review Board"), teams.stream().map(Team::getTeamName).toList());
        assertEquals(List.of("Ivy"), teams.getFirst().getInstructors().stream().map(User::getFirstName).toList());
        assertEquals(List.of("Noah"), teams.get(1).getInstructors().stream().map(User::getFirstName).toList());
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

        return new SeededData(sectionA.getSectionId(), "Review Board", "Pulse Analytics", "Gamma Studio", pulse.getTeamId(), review.getTeamId());
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

    private record SeededData(Long sectionAId, String teamBravo, String teamAlpha, String teamGamma, Long pulseAnalyticsId, Long reviewBoardId) {}
}

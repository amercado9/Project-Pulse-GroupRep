package team.projectpulse.team.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamInstructorAssignmentException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.InstructorAssignmentCandidate;
import team.projectpulse.team.dto.TeamInstructorAssignmentInput;
import team.projectpulse.team.dto.TeamInstructorAssignmentInstructor;
import team.projectpulse.team.dto.TeamInstructorAssignmentTeam;
import team.projectpulse.team.dto.TeamInstructorAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamInstructorAssignmentsRequest;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamInstructorAssignmentService {

    private static final Comparator<User> USER_COMPARATOR = Comparator
        .comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);

    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamInstructorAssignmentService(
        SectionRepository sectionRepository,
        TeamRepository teamRepository,
        UserRepository userRepository
    ) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public TeamInstructorAssignmentWorkspace loadWorkspace(Long sectionId) {
        Section section = requireSection(sectionId);
        List<Team> teams = teamRepository.findAllBySectionIdWithInstructorsOrdered(sectionId);
        List<User> instructors = userRepository.findEnabledInstructorsOrdered();
        return toWorkspace(section, teams, instructors);
    }

    @Transactional
    public TeamInstructorAssignmentWorkspace updateAssignments(UpdateTeamInstructorAssignmentsRequest request) {
        if (request == null) {
            throw new InvalidTeamInstructorAssignmentException("Assignment request is required.");
        }
        if (request.sectionId() == null) {
            throw new InvalidTeamInstructorAssignmentException("Section is required.");
        }

        Section section = requireSection(request.sectionId());
        List<Team> teams = teamRepository.findAllBySectionIdWithInstructorsOrdered(request.sectionId());
        List<User> eligibleInstructors = userRepository.findEnabledInstructorsOrdered();

        if (teams.isEmpty()) {
            throw new InvalidTeamInstructorAssignmentException("At least one team must exist in the selected section.");
        }
        if (eligibleInstructors.isEmpty()) {
            throw new InvalidTeamInstructorAssignmentException("At least one enabled instructor must exist before assignments can be saved.");
        }

        Map<Long, Team> teamsById = teams.stream()
            .collect(Collectors.toMap(Team::getTeamId, team -> team, (left, right) -> left, LinkedHashMap::new));
        Map<Long, User> instructorsById = eligibleInstructors.stream()
            .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left, LinkedHashMap::new));

        Map<Long, List<Long>> assignmentsByTeam = validateAssignments(
            request.assignments(),
            request.sectionId(),
            teamsById,
            instructorsById
        );

        for (Team team : teams) {
            team.setInstructors(new LinkedHashSet<>());
        }

        for (Map.Entry<Long, List<Long>> entry : assignmentsByTeam.entrySet()) {
            Team team = teamsById.get(entry.getKey());
            LinkedHashSet<User> assignedInstructors = entry.getValue().stream()
                .map(instructorsById::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            team.setInstructors(assignedInstructors);
        }

        teamRepository.saveAll(teams);

        List<Team> refreshedTeams = teamRepository.findAllBySectionIdWithInstructorsOrdered(request.sectionId());
        return toWorkspace(section, refreshedTeams, eligibleInstructors);
    }

    private Section requireSection(Long sectionId) {
        if (sectionId == null) {
            throw new InvalidTeamInstructorAssignmentException("Section is required.");
        }
        return sectionRepository.findById(sectionId)
            .orElseThrow(() -> new SectionNotFoundException(sectionId));
    }

    private Map<Long, List<Long>> validateAssignments(
        List<TeamInstructorAssignmentInput> assignments,
        Long sectionId,
        Map<Long, Team> teamsById,
        Map<Long, User> instructorsById
    ) {
        if (assignments == null || assignments.isEmpty()) {
            throw new InvalidTeamInstructorAssignmentException("Assignments are required.");
        }

        Set<Long> seenTeamIds = new LinkedHashSet<>();
        Map<Long, List<Long>> assignmentsByTeam = new LinkedHashMap<>();

        for (TeamInstructorAssignmentInput assignment : assignments) {
            if (assignment == null || assignment.teamId() == null) {
                throw new InvalidTeamInstructorAssignmentException("Each assignment must include a team.");
            }
            if (!seenTeamIds.add(assignment.teamId())) {
                throw new InvalidTeamInstructorAssignmentException("Each team may only appear once in the assignment payload.");
            }

            Team team = teamsById.get(assignment.teamId());
            if (team == null) {
                throw new InvalidTeamInstructorAssignmentException(
                    "Team " + assignment.teamId() + " does not belong to section " + sectionId + "."
                );
            }

            List<Long> instructorIds = assignment.instructorIds() == null ? List.of() : assignment.instructorIds();
            Set<Long> seenInstructorsInTeam = new LinkedHashSet<>();
            List<Long> normalizedInstructorIds = new ArrayList<>();

            for (Long instructorId : instructorIds) {
                if (instructorId == null) {
                    throw new InvalidTeamInstructorAssignmentException("Assigned instructor ids must not contain null values.");
                }
                if (!seenInstructorsInTeam.add(instructorId)) {
                    throw new InvalidTeamInstructorAssignmentException("An instructor may only appear once per team assignment.");
                }

                User instructor = instructorsById.get(instructorId);
                if (instructor == null || !isInstructor(instructor)) {
                    throw new InvalidTeamInstructorAssignmentException("Instructor " + instructorId + " is not an enabled instructor.");
                }

                normalizedInstructorIds.add(instructorId);
            }

            if (normalizedInstructorIds.isEmpty()) {
                throw new InvalidTeamInstructorAssignmentException(
                    "Each team in the selected section must be assigned at least one instructor."
                );
            }

            assignmentsByTeam.put(assignment.teamId(), normalizedInstructorIds);
        }

        if (!seenTeamIds.equals(teamsById.keySet())) {
            throw new InvalidTeamInstructorAssignmentException(
                "Assignments must be provided for every team in the selected section."
            );
        }

        return assignmentsByTeam;
    }

    private TeamInstructorAssignmentWorkspace toWorkspace(Section section, List<Team> teams, List<User> instructors) {
        Map<Long, List<String>> assignedTeamNamesByInstructorId = new LinkedHashMap<>();
        for (User instructor : instructors) {
            assignedTeamNamesByInstructorId.put(instructor.getId(), new ArrayList<>());
        }

        List<TeamInstructorAssignmentTeam> teamDtos = teams.stream()
            .sorted(Comparator.comparing(Team::getTeamName, String.CASE_INSENSITIVE_ORDER))
            .map(team -> {
                List<TeamInstructorAssignmentInstructor> assignedInstructors = team.getInstructors().stream()
                    .sorted(USER_COMPARATOR)
                    .map(instructor -> {
                        assignedTeamNamesByInstructorId
                            .computeIfAbsent(instructor.getId(), ignored -> new ArrayList<>())
                            .add(team.getTeamName());

                        return new TeamInstructorAssignmentInstructor(
                            instructor.getId(),
                            toDisplayName(instructor),
                            instructor.getEmail()
                        );
                    })
                    .toList();

                return new TeamInstructorAssignmentTeam(
                    team.getTeamId(),
                    team.getTeamName(),
                    assignedInstructors
                );
            })
            .toList();

        List<InstructorAssignmentCandidate> instructorCandidates = instructors.stream()
            .sorted(USER_COMPARATOR)
            .map(instructor -> new InstructorAssignmentCandidate(
                instructor.getId(),
                toDisplayName(instructor),
                instructor.getEmail(),
                assignedTeamNamesByInstructorId.getOrDefault(instructor.getId(), List.of()).stream()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList()
            ))
            .toList();

        return new TeamInstructorAssignmentWorkspace(
            section.getSectionId(),
            section.getSectionName(),
            teamDtos,
            instructorCandidates
        );
    }

    private String toDisplayName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }

    private boolean isInstructor(User user) {
        String roles = user.getRoles() == null ? "" : user.getRoles().toLowerCase();
        return List.of(roles.split("\\s+")).contains("instructor");
    }
}

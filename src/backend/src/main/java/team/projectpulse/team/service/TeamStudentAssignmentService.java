package team.projectpulse.team.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.section.domain.Section;
import team.projectpulse.section.domain.SectionNotFoundException;
import team.projectpulse.section.repository.SectionRepository;
import team.projectpulse.team.domain.InvalidTeamStudentAssignmentException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.dto.StudentAssignmentCandidate;
import team.projectpulse.team.dto.TeamStudentAssignmentInput;
import team.projectpulse.team.dto.TeamStudentAssignmentTeam;
import team.projectpulse.team.dto.TeamStudentAssignmentWorkspace;
import team.projectpulse.team.dto.UpdateTeamStudentAssignmentsRequest;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TeamStudentAssignmentService {

    private static final Comparator<User> USER_COMPARATOR = Comparator
        .comparing(User::getLastName, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(User::getFirstName, String.CASE_INSENSITIVE_ORDER)
        .thenComparing(User::getEmail, String.CASE_INSENSITIVE_ORDER);

    private final SectionRepository sectionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    public TeamStudentAssignmentService(
        SectionRepository sectionRepository,
        TeamRepository teamRepository,
        UserRepository userRepository
    ) {
        this.sectionRepository = sectionRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
    }

    public TeamStudentAssignmentWorkspace loadWorkspace(Long sectionId) {
        Section section = requireSection(sectionId);
        List<Team> teams = teamRepository.findAllBySectionIdWithStudentsOrdered(sectionId);
        List<User> students = userRepository.findEnabledStudentsBySectionId(sectionId);
        return toWorkspace(section, teams, students);
    }

    @Transactional
    public TeamStudentAssignmentWorkspace updateAssignments(UpdateTeamStudentAssignmentsRequest request) {
        if (request == null) {
            throw new InvalidTeamStudentAssignmentException("Assignment request is required.");
        }
        if (request.sectionId() == null) {
            throw new InvalidTeamStudentAssignmentException("Section is required.");
        }

        Section section = requireSection(request.sectionId());
        List<Team> teams = teamRepository.findAllBySectionIdWithStudentsOrdered(request.sectionId());
        List<User> eligibleStudents = userRepository.findEnabledStudentsBySectionId(request.sectionId());

        if (teams.isEmpty()) {
            throw new InvalidTeamStudentAssignmentException("At least one team must exist in the selected section.");
        }
        if (eligibleStudents.isEmpty()) {
            throw new InvalidTeamStudentAssignmentException("At least one eligible student must exist in the selected section.");
        }

        Map<Long, Team> teamsById = teams.stream()
            .collect(Collectors.toMap(Team::getTeamId, team -> team, (left, right) -> left, LinkedHashMap::new));
        Map<Long, User> studentsById = eligibleStudents.stream()
            .collect(Collectors.toMap(User::getId, user -> user, (left, right) -> left, LinkedHashMap::new));

        Map<Long, List<Long>> assignmentsByTeam = validateAssignments(
            request.assignments(),
            request.sectionId(),
            teamsById,
            studentsById
        );

        for (Team team : teams) {
            team.setStudents(new LinkedHashSet<>());
        }

        for (Map.Entry<Long, List<Long>> entry : assignmentsByTeam.entrySet()) {
            Team team = teamsById.get(entry.getKey());
            LinkedHashSet<User> assignedStudents = entry.getValue().stream()
                .map(studentsById::get)
                .collect(Collectors.toCollection(LinkedHashSet::new));
            team.setStudents(assignedStudents);
        }

        teamRepository.saveAll(teams);

        List<Team> refreshedTeams = teamRepository.findAllBySectionIdWithStudentsOrdered(request.sectionId());
        return toWorkspace(section, refreshedTeams, eligibleStudents);
    }

    private Section requireSection(Long sectionId) {
        if (sectionId == null) {
            throw new InvalidTeamStudentAssignmentException("Section is required.");
        }
        return sectionRepository.findById(sectionId)
            .orElseThrow(() -> new SectionNotFoundException(sectionId));
    }

    private Map<Long, List<Long>> validateAssignments(
        List<TeamStudentAssignmentInput> assignments,
        Long sectionId,
        Map<Long, Team> teamsById,
        Map<Long, User> studentsById
    ) {
        if (assignments == null || assignments.isEmpty()) {
            throw new InvalidTeamStudentAssignmentException("Assignments are required.");
        }

        Set<Long> seenTeamIds = new LinkedHashSet<>();
        Set<Long> seenStudentIds = new LinkedHashSet<>();
        Map<Long, List<Long>> assignmentsByTeam = new LinkedHashMap<>();

        for (TeamStudentAssignmentInput assignment : assignments) {
            if (assignment == null || assignment.teamId() == null) {
                throw new InvalidTeamStudentAssignmentException("Each assignment must include a team.");
            }
            if (!seenTeamIds.add(assignment.teamId())) {
                throw new InvalidTeamStudentAssignmentException("Each team may only appear once in the assignment payload.");
            }

            Team team = teamsById.get(assignment.teamId());
            if (team == null) {
                throw new InvalidTeamStudentAssignmentException("Team " + assignment.teamId() + " does not belong to section " + sectionId + ".");
            }

            List<Long> studentIds = assignment.studentIds() == null ? List.of() : assignment.studentIds();
            Set<Long> seenStudentsInTeam = new LinkedHashSet<>();
            List<Long> normalizedStudentIds = new ArrayList<>();

            for (Long studentId : studentIds) {
                if (studentId == null) {
                    throw new InvalidTeamStudentAssignmentException("Assigned student ids must not contain null values.");
                }
                if (!seenStudentsInTeam.add(studentId)) {
                    throw new InvalidTeamStudentAssignmentException("A student may only appear once per team assignment.");
                }

                User student = studentsById.get(studentId);
                if (student == null) {
                    throw new InvalidTeamStudentAssignmentException("Student " + studentId + " does not belong to section " + sectionId + ".");
                }
                if (!isStudent(student)) {
                    throw new InvalidTeamStudentAssignmentException("User " + studentId + " is not a student.");
                }
                if (!student.isEnabled()) {
                    throw new InvalidTeamStudentAssignmentException("Student " + studentId + " is not enabled.");
                }
                if (student.getSection() == null || !Objects.equals(student.getSection().getSectionId(), sectionId)) {
                    throw new InvalidTeamStudentAssignmentException("Student " + studentId + " does not belong to section " + sectionId + ".");
                }
                if (!seenStudentIds.add(studentId)) {
                    throw new InvalidTeamStudentAssignmentException("Each student may only be assigned to one team.");
                }

                normalizedStudentIds.add(studentId);
            }

            assignmentsByTeam.put(assignment.teamId(), normalizedStudentIds);
        }

        if (!seenTeamIds.equals(teamsById.keySet())) {
            throw new InvalidTeamStudentAssignmentException("Assignments must be provided for every team in the selected section.");
        }
        if (!seenStudentIds.equals(studentsById.keySet())) {
            throw new InvalidTeamStudentAssignmentException("Every eligible student in the selected section must be assigned to exactly one team.");
        }

        return assignmentsByTeam;
    }

    private TeamStudentAssignmentWorkspace toWorkspace(Section section, List<Team> teams, List<User> students) {
        List<StudentAssignmentCandidate> studentCandidates = students.stream()
            .sorted(USER_COMPARATOR)
            .map(this::toCandidate)
            .toList();

        List<TeamStudentAssignmentTeam> teamDtos = teams.stream()
            .sorted(Comparator.comparing(Team::getTeamName, String.CASE_INSENSITIVE_ORDER))
            .map(team -> new TeamStudentAssignmentTeam(
                team.getTeamId(),
                team.getTeamName(),
                team.getStudents().stream()
                    .sorted(USER_COMPARATOR)
                    .map(this::toCandidate)
                    .toList()
            ))
            .toList();

        return new TeamStudentAssignmentWorkspace(
            section.getSectionId(),
            section.getSectionName(),
            teamDtos,
            studentCandidates
        );
    }

    private StudentAssignmentCandidate toCandidate(User student) {
        return new StudentAssignmentCandidate(
            student.getId(),
            (student.getFirstName() + " " + student.getLastName()).trim(),
            student.getEmail()
        );
    }

    private boolean isStudent(User user) {
        String roles = user.getRoles() == null ? "" : user.getRoles().toLowerCase();
        return List.of(roles.split("\\s+")).contains("student");
    }
}

package team.projectpulse.report.service;

import org.springframework.stereotype.Service;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.report.dto.StudentWarReport;
import team.projectpulse.report.dto.TeamWarReportResponse;
import team.projectpulse.report.dto.WarReportRow;
import team.projectpulse.team.domain.InvalidTeamException;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TeamRepository teamRepository;
    private final ActivityRepository activityRepository;

    public ReportService(TeamRepository teamRepository, ActivityRepository activityRepository) {
        this.teamRepository = teamRepository;
        this.activityRepository = activityRepository;
    }

    public TeamWarReportResponse generateTeamWarReport(Long teamId, String week) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new InvalidTeamException("Team not found with id " + teamId));

        List<Activity> activities = activityRepository.findByTeamIdAndWeek(teamId, week);

        Map<Long, List<Activity>> byStudent = activities.stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        List<User> students = new ArrayList<>(team.getStudents());
        students.sort((a, b) -> {
            int cmp = a.getLastName().compareToIgnoreCase(b.getLastName());
            return cmp != 0 ? cmp : a.getFirstName().compareToIgnoreCase(b.getFirstName());
        });

        List<StudentWarReport> studentReports = students.stream()
                .map(student -> {
                    List<Activity> studentActivities = byStudent.getOrDefault(student.getId(), List.of());
                    boolean submitted = !studentActivities.isEmpty();
                    List<WarReportRow> rows = studentActivities.stream()
                            .map(a -> new WarReportRow(
                                    a.getCategory().name(),
                                    a.getPlannedActivity(),
                                    a.getDescription(),
                                    a.getPlannedHours(),
                                    a.getActualHours(),
                                    a.getStatus().name()
                            ))
                            .toList();
                    return new StudentWarReport(
                            student.getId(),
                            student.getFirstName() + " " + student.getLastName(),
                            submitted,
                            rows
                    );
                })
                .toList();

        return new TeamWarReportResponse(team.getTeamId(), team.getTeamName(), week, studentReports);
    }
}

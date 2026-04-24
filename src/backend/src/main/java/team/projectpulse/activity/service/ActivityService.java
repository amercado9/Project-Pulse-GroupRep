package team.projectpulse.activity.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.projectpulse.activity.domain.Activity;
import team.projectpulse.activity.domain.ActivityCategory;
import team.projectpulse.activity.domain.ActivityNotFoundException;
import team.projectpulse.activity.domain.ActivityStatus;
import team.projectpulse.activity.domain.InvalidActivityException;
import team.projectpulse.activity.dto.ActivityDetail;
import team.projectpulse.activity.dto.ActivityWeekOption;
import team.projectpulse.activity.dto.ActivityWorkspace;
import team.projectpulse.activity.dto.CreateActivityRequest;
import team.projectpulse.activity.dto.UpdateActivityRequest;
import team.projectpulse.activity.repository.ActivityRepository;
import team.projectpulse.section.domain.Section;
import team.projectpulse.team.domain.Team;
import team.projectpulse.team.repository.TeamRepository;
import team.projectpulse.user.domain.User;
import team.projectpulse.user.repository.UserRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ActivityService {

    private static final BigDecimal MAX_HOURS = new BigDecimal("999.99");
    private static final int MAX_PLANNED_ACTIVITY_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final WeekFields ISO = WeekFields.ISO;

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    public ActivityService(
        ActivityRepository activityRepository,
        UserRepository userRepository,
        TeamRepository teamRepository
    ) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public ActivityWorkspace loadWorkspace(String studentEmail, String requestedWeek) {
        User student = requireStudent(studentEmail);
        Section section = student.getSection();

        if (section == null) {
            return new ActivityWorkspace(
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                false,
                "You must belong to a senior design section before managing WAR activities."
            );
        }

        Optional<Team> teamOptional = teamRepository.findByStudentIdWithSection(student.getId());
        if (teamOptional.isEmpty()) {
            return new ActivityWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                null,
                null,
                null,
                buildWeekOptions(section),
                List.of(),
                false,
                "You must be assigned to a senior design team before managing WAR activities."
            );
        }

        Team team = teamOptional.get();
        List<ActivityWeekOption> availableWeeks = buildWeekOptions(section);
        String selectedWeek = determineSelectedWeek(availableWeeks, requestedWeek);

        if (selectedWeek == null) {
            return new ActivityWorkspace(
                section.getSectionId(),
                section.getSectionName(),
                team.getTeamId(),
                team.getTeamName(),
                null,
                availableWeeks,
                List.of(),
                false,
                "No eligible weeks are available yet."
            );
        }

        List<ActivityDetail> activities = activityRepository
            .findAllByStudentIdAndWeekOrderByActivityIdAsc(student.getId(), selectedWeek)
            .stream()
            .map(this::toDetail)
            .toList();

        return new ActivityWorkspace(
            section.getSectionId(),
            section.getSectionName(),
            team.getTeamId(),
            team.getTeamName(),
            selectedWeek,
            availableWeeks,
            activities,
            true,
            null
        );
    }

    @Transactional
    public ActivityDetail createActivity(String studentEmail, CreateActivityRequest request) {
        StudentContext context = requireStudentContext(studentEmail);
        validateCreateRequest(request, context.section());

        Activity activity = new Activity();
        activity.setTeam(context.team());
        activity.setStudent(context.student());
        activity.setWeek(normalizeWeek(request.week(), context.section()));
        activity.setCategory(requireCategory(request.category()));
        activity.setPlannedActivity(requirePlannedActivity(request.plannedActivity()));
        activity.setDescription(requireDescription(request.description()));
        activity.setPlannedHours(requireHours(request.plannedHours(), "Planned hours are required."));
        activity.setActualHours(requireHours(request.actualHours(), "Actual hours are required."));
        activity.setStatus(requireStatus(request.status()));

        return toDetail(activityRepository.save(activity));
    }

    @Transactional
    public ActivityDetail updateActivity(String studentEmail, Long activityId, UpdateActivityRequest request) {
        StudentContext context = requireStudentContext(studentEmail);
        Activity activity = activityRepository.findByActivityIdAndStudentId(activityId, context.student().getId())
            .orElseThrow(() -> new ActivityNotFoundException(activityId));

        validateUpdateRequest(request);

        activity.setCategory(requireCategory(request.category()));
        activity.setPlannedActivity(requirePlannedActivity(request.plannedActivity()));
        activity.setDescription(requireDescription(request.description()));
        activity.setPlannedHours(requireHours(request.plannedHours(), "Planned hours are required."));
        activity.setActualHours(requireHours(request.actualHours(), "Actual hours are required."));
        activity.setStatus(requireStatus(request.status()));

        return toDetail(activityRepository.save(activity));
    }

    @Transactional
    public void deleteActivity(String studentEmail, Long activityId) {
        StudentContext context = requireStudentContext(studentEmail);
        Activity activity = activityRepository.findByActivityIdAndStudentId(activityId, context.student().getId())
            .orElseThrow(() -> new ActivityNotFoundException(activityId));

        activityRepository.delete(activity);
    }

    private User requireStudent(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
            .orElseThrow(() -> new InvalidActivityException("No student account found for the current user."));

        if (!isStudent(student)) {
            throw new InvalidActivityException("Only students can manage WAR activities.");
        }

        return student;
    }

    private StudentContext requireStudentContext(String studentEmail) {
        User student = requireStudent(studentEmail);
        Section section = student.getSection();
        if (section == null) {
            throw new InvalidActivityException("You must belong to a senior design section before managing WAR activities.");
        }

        Team team = teamRepository.findByStudentIdWithSection(student.getId())
            .orElseThrow(() -> new InvalidActivityException("You must be assigned to a senior design team before managing WAR activities."));

        List<ActivityWeekOption> availableWeeks = buildWeekOptions(section);
        if (availableWeeks.isEmpty()) {
            throw new InvalidActivityException("No eligible week is available for WAR activity management.");
        }

        return new StudentContext(student, section, team, availableWeeks);
    }

    private void validateCreateRequest(CreateActivityRequest request, Section section) {
        if (request == null) {
            throw new InvalidActivityException("Activity request is required.");
        }
        normalizeWeek(request.week(), section);
        requireCategory(request.category());
        requirePlannedActivity(request.plannedActivity());
        requireDescription(request.description());
        requireHours(request.plannedHours(), "Planned hours are required.");
        requireHours(request.actualHours(), "Actual hours are required.");
        requireStatus(request.status());
    }

    private void validateUpdateRequest(UpdateActivityRequest request) {
        if (request == null) {
            throw new InvalidActivityException("Activity request is required.");
        }
        requireCategory(request.category());
        requirePlannedActivity(request.plannedActivity());
        requireDescription(request.description());
        requireHours(request.plannedHours(), "Planned hours are required.");
        requireHours(request.actualHours(), "Actual hours are required.");
        requireStatus(request.status());
    }

    private String normalizeWeek(String week, Section section) {
        if (week == null || week.isBlank()) {
            throw new InvalidActivityException("Week is required.");
        }

        String trimmedWeek = week.trim();
        if (isFutureWeek(trimmedWeek)) {
            throw new InvalidActivityException("Future weeks cannot be selected.");
        }

        Set<String> allowedWeeks = buildWeekOptions(section).stream()
            .map(ActivityWeekOption::week)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (!allowedWeeks.contains(trimmedWeek)) {
            throw new InvalidActivityException("Selected week is not eligible for WAR activity management.");
        }

        return trimmedWeek;
    }

    private ActivityCategory requireCategory(ActivityCategory category) {
        if (category == null) {
            throw new InvalidActivityException("Activity category is required.");
        }
        return category;
    }

    private ActivityStatus requireStatus(ActivityStatus status) {
        if (status == null) {
            throw new InvalidActivityException("Status is required.");
        }
        return status;
    }

    private String requirePlannedActivity(String plannedActivity) {
        String normalized = normalizeText(plannedActivity);
        if (normalized == null) {
            throw new InvalidActivityException("Planned activity is required.");
        }
        if (normalized.length() > MAX_PLANNED_ACTIVITY_LENGTH) {
            throw new InvalidActivityException("Planned activity must be 255 characters or fewer.");
        }
        return normalized;
    }

    private String requireDescription(String description) {
        String normalized = normalizeText(description);
        if (normalized == null) {
            throw new InvalidActivityException("Activity description is required.");
        }
        if (normalized.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidActivityException("Activity description must be 2000 characters or fewer.");
        }
        return normalized;
    }

    private BigDecimal requireHours(BigDecimal hours, String requiredMessage) {
        if (hours == null) {
            throw new InvalidActivityException(requiredMessage);
        }
        if (hours.compareTo(BigDecimal.ZERO) < 0) {
            if ("Planned hours are required.".equals(requiredMessage)) {
                throw new InvalidActivityException("Planned hours must be zero or greater.");
            }
            throw new InvalidActivityException("Actual hours must be zero or greater.");
        }
        if (hours.compareTo(MAX_HOURS) > 0) {
            if ("Planned hours are required.".equals(requiredMessage)) {
                throw new InvalidActivityException("Planned hours must be 999.99 or fewer.");
            }
            throw new InvalidActivityException("Actual hours must be 999.99 or fewer.");
        }
        return hours.stripTrailingZeros();
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String determineSelectedWeek(List<ActivityWeekOption> availableWeeks, String requestedWeek) {
        if (availableWeeks.isEmpty()) {
            return null;
        }
        if (requestedWeek != null && availableWeeks.stream().anyMatch(option -> option.week().equals(requestedWeek))) {
            return requestedWeek;
        }
        return availableWeeks.stream()
            .map(ActivityWeekOption::week)
            .max(String.CASE_INSENSITIVE_ORDER)
            .orElse(null);
    }

    private List<ActivityWeekOption> buildWeekOptions(Section section) {
        Set<String> activeWeeks = new LinkedHashSet<>(section.getActiveWeeks() == null ? List.of() : section.getActiveWeeks());
        List<String> eligibleWeeks = deriveEligibleWeeks(section);

        return eligibleWeeks.stream()
            .sorted(Comparator.naturalOrder())
            .map(week -> new ActivityWeekOption(week, formatWeekLabel(week), activeWeeks.contains(week)))
            .toList();
    }

    private List<String> deriveEligibleWeeks(Section section) {
        if (section.getStartDate() != null && section.getEndDate() != null) {
            LocalDate currentDate = LocalDate.now();
            LocalDate cappedEndDate = section.getEndDate().isBefore(currentDate) ? section.getEndDate() : currentDate;

            if (cappedEndDate.isBefore(section.getStartDate())) {
                return List.of();
            }

            LocalDate cursor = toMonday(section.getStartDate());
            LocalDate endCursor = toMonday(cappedEndDate);
            List<String> weeks = new ArrayList<>();

            while (!cursor.isAfter(endCursor)) {
                weeks.add(toIsoWeek(cursor));
                cursor = cursor.plusWeeks(1);
            }
            return weeks;
        }

        return (section.getActiveWeeks() == null ? List.<String>of() : section.getActiveWeeks()).stream()
            .filter(week -> !isFutureWeek(week))
            .sorted()
            .toList();
    }

    private boolean isFutureWeek(String isoWeek) {
        try {
            LocalDate weekStart = parseIsoWeekStart(isoWeek);
            return weekStart.isAfter(toMonday(LocalDate.now()));
        } catch (RuntimeException ex) {
            return true;
        }
    }

    private LocalDate parseIsoWeekStart(String isoWeek) {
        if (isoWeek == null || !isoWeek.matches("\\d{4}-W\\d{2}")) {
            throw new IllegalArgumentException("Invalid ISO week.");
        }
        String[] parts = isoWeek.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        return LocalDate.of(year, 1, 4)
            .with(ISO.weekOfWeekBasedYear(), week)
            .with(ISO.dayOfWeek(), 1);
    }

    private LocalDate toMonday(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    private String toIsoWeek(LocalDate date) {
        int weekYear = date.get(ISO.weekBasedYear());
        int weekNumber = date.get(ISO.weekOfWeekBasedYear());
        return "%d-W%02d".formatted(weekYear, weekNumber);
    }

    private String formatWeekLabel(String isoWeek) {
        String[] parts = isoWeek.split("-W");
        return "Week " + Integer.parseInt(parts[1]) + " (" + parts[0] + ")";
    }

    private ActivityDetail toDetail(Activity activity) {
        return new ActivityDetail(
            activity.getActivityId(),
            activity.getWeek(),
            activity.getCategory(),
            activity.getPlannedActivity(),
            activity.getDescription(),
            activity.getPlannedHours(),
            activity.getActualHours(),
            activity.getStatus()
        );
    }

    private boolean isStudent(User user) {
        String roles = user.getRoles() == null ? "" : user.getRoles().toLowerCase(Locale.ROOT);
        return List.of(roles.split("\\s+")).contains("student");
    }

    private record StudentContext(User student, Section section, Team team, List<ActivityWeekOption> availableWeeks) {
    }
}

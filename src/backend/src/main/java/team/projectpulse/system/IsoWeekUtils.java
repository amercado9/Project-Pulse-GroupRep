package team.projectpulse.system;

import team.projectpulse.evaluation.domain.InvalidPeerEvaluationException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;

public final class IsoWeekUtils {

    private static final WeekFields ISO = WeekFields.ISO;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    private IsoWeekUtils() {
    }

    public static String toIsoWeek(LocalDate date) {
        int weekYear = date.get(ISO.weekBasedYear());
        int weekNumber = date.get(ISO.weekOfWeekBasedYear());
        return "%d-W%02d".formatted(weekYear, weekNumber);
    }

    public static LocalDate parseIsoWeekStart(String isoWeek) {
        if (isoWeek == null || !isoWeek.matches("\\d{4}-W\\d{2}")) {
            throw new InvalidPeerEvaluationException("Invalid ISO week format.");
        }
        String[] parts = isoWeek.split("-W");
        int year = Integer.parseInt(parts[0]);
        int week = Integer.parseInt(parts[1]);
        return LocalDate.of(year, 1, 4)
            .with(ISO.weekOfWeekBasedYear(), week)
            .with(ISO.dayOfWeek(), 1);
    }

    public static String formatWeekLabel(String isoWeek) {
        String[] parts = isoWeek.split("-W");
        return "Week " + Integer.parseInt(parts[1]) + " (" + parts[0] + ")";
    }

    public static String formatWeekRangeLabel(String isoWeek) {
        LocalDate start = parseIsoWeekStart(isoWeek);
        LocalDate end = start.plusDays(6);
        return DATE_FORMAT.format(start) + " -- " + DATE_FORMAT.format(end);
    }

    public static LocalDate toMonday(LocalDate date) {
        return date.with(DayOfWeek.MONDAY);
    }

    public static Comparator<String> descendingComparator() {
        return Comparator.comparing(IsoWeekUtils::parseIsoWeekStart).reversed();
    }

    public static List<String> sortDescending(List<String> isoWeeks) {
        return isoWeeks.stream()
            .sorted(descendingComparator())
            .toList();
    }
}

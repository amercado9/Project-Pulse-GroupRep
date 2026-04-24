package team.projectpulse.activity.domain;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException(Long activityId) {
        super("No activity found with id: " + activityId);
    }
}

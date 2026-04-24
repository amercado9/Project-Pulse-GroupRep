package team.projectpulse.activity.domain;

import jakarta.persistence.*;
import team.projectpulse.team.domain.Team;
import team.projectpulse.user.domain.User;

import java.math.BigDecimal;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String week;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityCategory category;

    @Column(name = "planned_activity", nullable = false)
    private String plannedActivity;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "planned_hours", nullable = false)
    private BigDecimal plannedHours;

    @Column(name = "actual_hours", nullable = false)
    private BigDecimal actualHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus status;

    public Long getActivityId()               { return activityId; }
    public void setActivityId(Long v)         { this.activityId = v; }

    public User getStudent()                  { return student; }
    public void setStudent(User v)            { this.student = v; }

    public Team getTeam()                     { return team; }
    public void setTeam(Team v)               { this.team = v; }

    public String getWeek()                   { return week; }
    public void setWeek(String v)             { this.week = v; }

    public ActivityCategory getCategory()     { return category; }
    public void setCategory(ActivityCategory v) { this.category = v; }

    public String getPlannedActivity()        { return plannedActivity; }
    public void setPlannedActivity(String v)  { this.plannedActivity = v; }

    public String getDescription()            { return description; }
    public void setDescription(String v)      { this.description = v; }

    public BigDecimal getPlannedHours()       { return plannedHours; }
    public void setPlannedHours(BigDecimal v) { this.plannedHours = v; }

    public BigDecimal getActualHours()        { return actualHours; }
    public void setActualHours(BigDecimal v)  { this.actualHours = v; }

    public ActivityStatus getStatus()         { return status; }
    public void setStatus(ActivityStatus v)   { this.status = v; }
}

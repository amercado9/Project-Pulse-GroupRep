package team.projectpulse.section;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sections")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "section_name", nullable = false)
    private String sectionName;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Column(name = "rubric_id")
    private Long rubricId;

    @Column(name = "course_id")
    private Long courseId;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "war_weekly_due_day")
    private String warWeeklyDueDay = "FRIDAY";

    @Column(name = "war_due_time")
    private String warDueTime = "23:59";

    @Column(name = "peer_evaluation_weekly_due_day")
    private String peerEvaluationWeeklyDueDay = "FRIDAY";

    @Column(name = "peer_evaluation_due_time")
    private String peerEvaluationDueTime = "23:59";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "section_active_weeks", joinColumns = @JoinColumn(name = "section_id"))
    @Column(name = "week")
    private List<String> activeWeeks;

    // ── Getters / Setters ────────────────────────────────────────────────────

    public Long getSectionId()                          { return sectionId; }
    public void setSectionId(Long v)                    { this.sectionId = v; }

    public String getSectionName()                      { return sectionName; }
    public void setSectionName(String v)                { this.sectionName = v; }

    public LocalDate getStartDate()                     { return startDate; }
    public void setStartDate(LocalDate v)               { this.startDate = v; }

    public LocalDate getEndDate()                       { return endDate; }
    public void setEndDate(LocalDate v)                 { this.endDate = v; }

    public Long getRubricId()                           { return rubricId; }
    public void setRubricId(Long v)                     { this.rubricId = v; }

    public Long getCourseId()                           { return courseId; }
    public void setCourseId(Long v)                     { this.courseId = v; }

    @com.fasterxml.jackson.annotation.JsonProperty("isActive")
    public boolean isActive()                           { return isActive; }
    public void setActive(boolean v)                    { this.isActive = v; }

    public String getWarWeeklyDueDay()                  { return warWeeklyDueDay; }
    public void setWarWeeklyDueDay(String v)            { this.warWeeklyDueDay = v; }

    public String getWarDueTime()                       { return warDueTime; }
    public void setWarDueTime(String v)                 { this.warDueTime = v; }

    public String getPeerEvaluationWeeklyDueDay()       { return peerEvaluationWeeklyDueDay; }
    public void setPeerEvaluationWeeklyDueDay(String v) { this.peerEvaluationWeeklyDueDay = v; }

    public String getPeerEvaluationDueTime()            { return peerEvaluationDueTime; }
    public void setPeerEvaluationDueTime(String v)      { this.peerEvaluationDueTime = v; }

    public List<String> getActiveWeeks()                { return activeWeeks; }
    public void setActiveWeeks(List<String> v)          { this.activeWeeks = v; }
}

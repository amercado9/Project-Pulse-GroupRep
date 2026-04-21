package team.projectpulse.team.domain;

import jakarta.persistence.*;
import team.projectpulse.section.domain.Section;
import team.projectpulse.user.domain.User;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "team_name", nullable = false, unique = true)
    private String teamName;

    @Column(name = "team_description")
    private String teamDescription;

    @Column(name = "team_website_url")
    private String teamWebsiteUrl;

    @ManyToMany
    @JoinTable(
        name = "team_students",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> students = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
        name = "team_instructors",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> instructors = new LinkedHashSet<>();

    public Long getTeamId()                         { return teamId; }
    public void setTeamId(Long v)                   { this.teamId = v; }

    public Section getSection()                     { return section; }
    public void setSection(Section v)               { this.section = v; }

    public String getTeamName()                     { return teamName; }
    public void setTeamName(String v)               { this.teamName = v; }

    public String getTeamDescription()              { return teamDescription; }
    public void setTeamDescription(String v)        { this.teamDescription = v; }

    public String getTeamWebsiteUrl()               { return teamWebsiteUrl; }
    public void setTeamWebsiteUrl(String v)         { this.teamWebsiteUrl = v; }

    public Set<User> getStudents()                  { return students; }
    public void setStudents(Set<User> v)            { this.students = v; }

    public Set<User> getInstructors()               { return instructors; }
    public void setInstructors(Set<User> v)         { this.instructors = v; }
}

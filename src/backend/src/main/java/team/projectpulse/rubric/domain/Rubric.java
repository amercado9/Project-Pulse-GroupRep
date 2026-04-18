package team.projectpulse.rubric.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rubrics")
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rubric_id")
    private Long rubricId;

    @Column(name = "rubric_name", nullable = false, unique = true)
    private String rubricName;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "rubric_criteria",
        joinColumns = @JoinColumn(name = "rubric_id"),
        inverseJoinColumns = @JoinColumn(name = "criterion_id")
    )
    private List<Criterion> criteria = new ArrayList<>();

    public Long getRubricId()              { return rubricId; }
    public void setRubricId(Long v)        { this.rubricId = v; }

    public String getRubricName()          { return rubricName; }
    public void setRubricName(String v)    { this.rubricName = v; }

    public List<Criterion> getCriteria()          { return criteria; }
    public void setCriteria(List<Criterion> v)    { this.criteria = v; }
}

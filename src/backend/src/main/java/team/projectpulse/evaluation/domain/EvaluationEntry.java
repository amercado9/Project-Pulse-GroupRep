package team.projectpulse.evaluation.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import team.projectpulse.user.domain.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evaluation_entries")
public class EvaluationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "entry_id")
    private Long entryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private EvaluationSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluatee_student_id", nullable = false)
    private User evaluateeStudent;

    @Column(name = "public_comment", columnDefinition = "TEXT")
    private String publicComment;

    @Column(name = "private_comment", columnDefinition = "TEXT")
    private String privateComment;

    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EvaluationScore> scores = new ArrayList<>();

    public void addScore(EvaluationScore score) {
        score.setEntry(this);
        scores.add(score);
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public EvaluationSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(EvaluationSubmission submission) {
        this.submission = submission;
    }

    public User getEvaluateeStudent() {
        return evaluateeStudent;
    }

    public void setEvaluateeStudent(User evaluateeStudent) {
        this.evaluateeStudent = evaluateeStudent;
    }

    public String getPublicComment() {
        return publicComment;
    }

    public void setPublicComment(String publicComment) {
        this.publicComment = publicComment;
    }

    public String getPrivateComment() {
        return privateComment;
    }

    public void setPrivateComment(String privateComment) {
        this.privateComment = privateComment;
    }

    public List<EvaluationScore> getScores() {
        return scores;
    }

    public void setScores(List<EvaluationScore> scores) {
        this.scores = scores;
    }
}

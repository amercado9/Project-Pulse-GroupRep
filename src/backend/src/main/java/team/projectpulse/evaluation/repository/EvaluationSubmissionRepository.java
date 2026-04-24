package team.projectpulse.evaluation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.evaluation.domain.EvaluationSubmission;

import java.util.Optional;

public interface EvaluationSubmissionRepository extends JpaRepository<EvaluationSubmission, Long> {

    default Optional<EvaluationSubmission> findByEvaluatorStudentIdAndWeek(Long evaluatorStudentId, String week) {
        Optional<EvaluationSubmission> submission = findSubmissionByEvaluatorStudentIdAndWeek(evaluatorStudentId, week);
        submission.ifPresent(found -> loadScores(found.getSubmissionId()));
        return submission;
    }

    default Optional<EvaluationSubmission> findBySubmissionIdAndEvaluatorStudentId(Long submissionId, Long evaluatorStudentId) {
        Optional<EvaluationSubmission> submission = findSubmissionBySubmissionIdAndEvaluatorStudentId(submissionId, evaluatorStudentId);
        submission.ifPresent(found -> loadScores(found.getSubmissionId()));
        return submission;
    }

    @Query("""
        select distinct s from EvaluationSubmission s
        join fetch s.team t
        join fetch s.evaluatorStudent evaluator
        left join fetch s.entries entry
        left join fetch entry.evaluateeStudent evaluatee
        where evaluator.id = :evaluatorStudentId
          and s.week = :week
        """)
    Optional<EvaluationSubmission> findSubmissionByEvaluatorStudentIdAndWeek(
        @Param("evaluatorStudentId") Long evaluatorStudentId,
        @Param("week") String week
    );

    @Query("""
        select distinct s from EvaluationSubmission s
        join fetch s.team t
        join fetch s.evaluatorStudent evaluator
        left join fetch s.entries entry
        left join fetch entry.evaluateeStudent evaluatee
        where s.submissionId = :submissionId
          and evaluator.id = :evaluatorStudentId
        """)
    Optional<EvaluationSubmission> findSubmissionBySubmissionIdAndEvaluatorStudentId(
        @Param("submissionId") Long submissionId,
        @Param("evaluatorStudentId") Long evaluatorStudentId
    );

    @Query("""
        select distinct entry from EvaluationEntry entry
        left join fetch entry.scores score
        left join fetch score.criterion criterion
        where entry.submission.submissionId = :submissionId
        """)
    java.util.List<team.projectpulse.evaluation.domain.EvaluationEntry> loadScores(@Param("submissionId") Long submissionId);
}

package team.projectpulse.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.activity.domain.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("""
        select a from Activity a
        join fetch a.team t
        join fetch a.student s
        where s.id = :studentId
          and a.week = :week
        order by a.activityId asc
        """)
    List<Activity> findAllByStudentIdAndWeekOrderByActivityIdAsc(
        @Param("studentId") Long studentId,
        @Param("week") String week
    );

    @Query("""
        select a from Activity a
        join fetch a.team t
        join fetch a.student s
        where a.activityId = :activityId
          and s.id = :studentId
        """)
    Optional<Activity> findByActivityIdAndStudentId(
        @Param("activityId") Long activityId,
        @Param("studentId") Long studentId
    );
}

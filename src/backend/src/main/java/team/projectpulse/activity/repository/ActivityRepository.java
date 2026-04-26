package team.projectpulse.activity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.activity.domain.Activity;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findAllByStudentIdAndWeekOrderByActivityIdAsc(Long studentId, String week);

    Optional<Activity> findByActivityIdAndStudentId(Long activityId, Long studentId);

    @Query("SELECT a FROM Activity a JOIN FETCH a.student WHERE a.team.teamId = :teamId AND a.week = :week ORDER BY a.student.lastName ASC, a.student.firstName ASC")
    List<Activity> findByTeamIdAndWeek(@Param("teamId") Long teamId, @Param("week") String week);

    @Query("SELECT a FROM Activity a WHERE a.student.id = :studentId AND a.team.teamId = :teamId AND a.week >= :startWeek AND a.week <= :endWeek ORDER BY a.week ASC, a.activityId ASC")
    List<Activity> findByStudentIdAndTeamIdAndWeekRange(
            @Param("studentId") Long studentId,
            @Param("teamId") Long teamId,
            @Param("startWeek") String startWeek,
            @Param("endWeek") String endWeek
    );
}

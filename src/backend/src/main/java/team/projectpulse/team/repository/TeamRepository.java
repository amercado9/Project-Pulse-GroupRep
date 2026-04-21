package team.projectpulse.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.team.domain.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("""
        select distinct t from Team t
        join fetch t.section s
        left join fetch t.students st
        left join fetch t.instructors i
        where (:sectionId is null or s.sectionId = :sectionId)
          and (:sectionName is null or lower(s.sectionName) like lower(concat('%', :sectionName, '%')))
          and (:teamName is null or lower(t.teamName) like lower(concat('%', :teamName, '%')))
          and (
              :instructor is null
              or lower(concat(coalesce(i.firstName, ''), ' ', coalesce(i.lastName, ''))) like lower(concat('%', :instructor, '%'))
              or lower(coalesce(i.firstName, '')) like lower(concat('%', :instructor, '%'))
              or lower(coalesce(i.lastName, '')) like lower(concat('%', :instructor, '%'))
          )
        order by s.sectionName desc, t.teamName asc
        """)
    List<Team> search(
        @Param("sectionId") Long sectionId,
        @Param("sectionName") String sectionName,
        @Param("teamName") String teamName,
        @Param("instructor") String instructor
    );

    @Query("""
        select distinct t from Team t
        join fetch t.section s
        left join fetch t.students st
        left join fetch t.instructors i
        where t.teamId = :teamId
        """)
    Optional<Team> findDetailById(@Param("teamId") Long teamId);
}

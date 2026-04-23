package team.projectpulse.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team.projectpulse.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
        select u from User u
        left join fetch u.section s
        where u.enabled = true
          and s.sectionId = :sectionId
          and lower(concat(' ', coalesce(u.roles, ''), ' ')) like '% student %'
        order by lower(u.lastName), lower(u.firstName), lower(u.email)
        """)
    List<User> findEnabledStudentsBySectionId(@Param("sectionId") Long sectionId);

    @Query("""
        select distinct u from User u
        where u.roles like '%student%'
          and (:firstName is null or lower(u.firstName) like lower(concat('%', :firstName, '%')))
          and (:lastName  is null or lower(u.lastName)  like lower(concat('%', :lastName,  '%')))
          and (:email     is null or lower(u.email)     like lower(concat('%', :email,     '%')))
          and (
              (:teamId is null and :teamName is null and :sectionId is null and :sectionName is null)
              or exists (
                  select t from Team t
                  join t.students s
                  join t.section sec
                  where s = u
                    and (:teamId      is null or t.teamId                                              = :teamId)
                    and (:teamName    is null or lower(t.teamName)                                     like lower(concat('%', :teamName,    '%')))
                    and (:sectionId   is null or sec.sectionId                                         = :sectionId)
                    and (:sectionName is null or lower(sec.sectionName)                                like lower(concat('%', :sectionName, '%')))
              )
          )
        order by u.lastName asc
        """)
    List<User> searchStudents(
            @Param("firstName")   String firstName,
            @Param("lastName")    String lastName,
            @Param("email")       String email,
            @Param("teamId")      Long   teamId,
            @Param("teamName")    String teamName,
            @Param("sectionId")   Long   sectionId,
            @Param("sectionName") String sectionName
    );
}

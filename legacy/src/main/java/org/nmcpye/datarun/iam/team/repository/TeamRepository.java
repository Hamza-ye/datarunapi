package org.nmcpye.datarun.iam.team.repository;

import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Team entity.
 *
 * @author Hamza Assada
 * @since 06/06/2023
 */
@Repository
public interface TeamRepository
    extends TeamRepositoryWithBagRelationships,
    JpaIdentifiableRepository<Team> {

    Optional<Team> findByCodeAndActivityUid(String code, String activityUid);

    default Optional<Team> findByUidWithEagerRelation(String uid) {
        return this.findByUid(uid).flatMap(this::fetchBagRelationships);
    }

    default Optional<Team> findOneWithEagerRelationshipsByUser(Long id) {
        return this.findOneWithToOneRelationshipsByUser(id);
    }

    default Page<Team> findAllWithEagerRelationshipsByUser(Pageable pageable) {
        return this.findAllWithToOneRelationshipsByUser(pageable);
    }

    @Query(
        value = "select team from Team team " +
            "left join team.activity " +
            "left join team.users user " +
            "where user.login = ?#{authentication.name}"
    )
    List<Team> findAllWithEagerRelation();

    @Query(
        "select team from Team team " +
            "left join team.activity a " +
            "left join team.users user " +
            "where a.uid =:activity and user.login =:userLogin and a.disabled = false"
    )
    List<Team> findFirstByActivityAndUser(@Param("activity") String activity,
                                          @Param("userLogin") String userLogin);

    @Query(
        "select team from Team team " +
            "join team.activity a " +
            "join team.users user " +
            "where (:includeDisabled = true OR team.disabled = false) and user.login =:login " +
            "and (:includeDisabled = true OR a.disabled = false)"
    )
    List<Team> findAllByUserLogin(@Param("login") String userLogin, boolean includeDisabled);


    @Query(
        "select team from Team team " +
            "join team.activity a " +
            "join team.users user " +
            "where (:includeDisabled = true OR team.disabled = false) and user.login =:login " +
            "and (:includeDisabled = true OR a.disabled = false)"
    )
    @EntityGraph(attributePaths = {"teamFormAccesses"})
    List<Team> findAllByUserWithFormAccesses(@Param("login") String userLogin, boolean includeDisabled);

    @Query(
        value = "select team from Team team " +
            "left join fetch team.activity " +
            "left join team.users u " +
            "where u.login = ?#{authentication.name}",
        countQuery = "select count(team) from Team team " +
            "left join team.users u " +
            "where u.login = ?#{authentication.name}"
    )
    Page<Team> findAllWithToOneRelationshipsByUser(Pageable pageable);

    @Query(
        "select team from Team team " +
            "left join fetch team.activity " +
            "left join team.users user " +
            "where team.id =:id and user.login = ?#{authentication.name}"
    )
    Optional<Team> findOneWithToOneRelationshipsByUser(@Param("id") Long id);
}

package org.nmcpye.datarun.assignment.assignment.repository;

import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.party.dto.AssignmentManifestProjection;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository
                extends JpaIdentifiableRepository<Assignment>, AssignmentRepositoryWithBagRelationships {

        @Query("select assignment from Assignment assignment " +
                        "left join  assignment.team t " +
                        "left join assignment.activity a " +
                        "where t.uid IN :teamUIDs and (:includeDisabled = true OR t.disabled = false) " +
                        "and (:includeDisabled = true OR a.disabled = false)")
        List<Assignment> findAllByTeams(@Param("teamUIDs") Collection<String> teamUIDs,
                        @Param("includeDisabled") boolean includeDisabled);

        List<Assignment> findAllByTeamUidIn(Collection<String> teamUIDs);

        // @Query("select assignment from Assignment assignment where assignment.path
        // like concat(:path, '%')")
        List<Assignment> findAllByPathContaining(String path);

        @Query("select assignment from Assignment assignment " +
                        "left join  assignment.team t " +
                        "left join assignment.orgUnit ou " +
                        "left join assignment.activity a " +
                        "where t.uid =:team and ou.uid =:orgUnit and a.uid =:activity order by a.id Limit 1")
        Optional<Assignment> findFirstByTeamAndOrgUnit(@Param("team") String team,
                        @Param("orgUnit") String orgUnit, @Param("activity") String activity);

        @Query(value = "select assignment from Assignment assignment " +
                        "left join assignment.activity " +
                        "left join assignment.orgUnit " +
                        "left join assignment.team " +
                        "join assignment.team.users user " +
                        "where assignment.activity.disabled =:disabled and " +
                        "assignment.team.disabled =:disabled and user.login = ?#{authentication.name}")
        List<Assignment> findAllByStatusUser(@Param("disabled") boolean disabled);

        Page<Assignment> findAllByPathIsNull(Pageable pageable);

        /// / new test
        @Query("SELECT ou.id FROM Assignment ou WHERE ou.parent.id IN :parentIds")
        List<Long> findChildIdsByParentIds(@Param("parentIds") List<Long> parentIds);

        @Query(value = """
                        SELECT DISTINCT assi.*
                        FROM assignment assi
                        WHERE assi.uid IN (
                            SELECT unnest(string_to_array(acc.path, ','))
                            FROM assignment acc
                            WHERE acc.id IN :accessibleIds
                        )
                        """, nativeQuery = true)
        Page<Assignment> findWithAncestors(@Param("accessibleIds") List<Long> accessibleIds, Pageable pageable);

        @Query(value = """
                        SELECT assi.*
                        FROM assignment assi
                        WHERE EXISTS (
                            SELECT 1
                            FROM assignment acc
                            WHERE acc.id IN :accessibleIds
                            AND assi.path LIKE CONCAT(acc.path, ',%')
                        )
                        """, nativeQuery = true)
        List<Assignment> findWithDescendants(@Param("accessibleIds") List<Long> accessibleIds);

        Page<Assignment> findAllByIdIn(Collection<String> ids, Pageable pageable);

        Page<Assignment> findAllByIdInAndLastModifiedDateAfter(Collection<String> ids, Instant lastModifiedDateAfter,
                        Pageable pageable);

        // @Query(value = """
        // SELECT assi.*
        // FROM assignment assi
        // WHERE EXISTS (
        // SELECT 1
        // FROM assignment acc
        // WHERE acc.id IN :accessibleIds
        // AND assi.path LIKE CONCAT(acc.path, ',%')
        // )
        // """, nativeQuery = true)
        // List<AssignmentSummary> findSummariesDescendants(@Param("accessibleIds")
        // List<Long> accessibleIds);
        //
        // @Query(value = """
        // SELECT assi.*
        // FROM assignment assi
        // Join team t on t.id = assi.team_id
        // WHERE t.uid IN :teamIds
        // """, nativeQuery = true)
        // Page<AssignmentSummary> findSummariesTeam(@Param("teamIds")
        // Collection<String> teamIds, Pageable pageable);
        //

        @Query("SELECT a.id as assignmentId, a.uid as assignmentUid, a.name as label, a.activity.uid as activityUid, " +
                        "a.orgUnit.uid as orgUnitUid, " +
                        "a.team.uid as teamUid, a.deleted, a.startDay, " +
                        "a.status, a.forms, a.lastModifiedDate " +
                        "FROM Assignment a " +
                        // "LEFT JOIN a.activity LEFT JOIN a.team LEFT JOIN a.orgUnit " +
                        "WHERE a.uid IN :uids ")
        List<AssignmentManifestProjection> findAssignmentManifestsByUids(@Param("uids") List<String> uids);

        // @Query(
        // value = "select assignment from Assignment assignment " +
        // "left join assignment.activity " +
        // "left join assignment.orgUnit " +
        // "left join assignment.team " +
        // "join assignment.team.users user " +
        // "where assignment.activity.disabled =:disabled and " +
        // "assignment.team.disabled =:disabled and user.login =
        // ?#{authentication.name}",
        // countQuery = "select count(assignment) from Assignment assignment " +
        // "join assignment.team.users user " +
        // "where assignment.activity.disabled =:disabled and " +
        // "assignment.team.disabled =:disabled and user.login =
        // ?#{authentication.name}"
        // )
        // Page<Assignment> findAllByStatusAndUser(@Param("disabled") boolean disabled,
        // Pageable pageable);

        // @Query(
        // value = "select assignment from Assignment assignment " +
        // "left join assignment.activity " +
        // "left join assignment.orgUnit " +
        // "left join assignment.team " +
        // "join assignment.team.users user " +
        // "where user.login = ?#{authentication.name}",
        // countQuery = "select count(assignment) from Assignment assignment " +
        // "join assignment.team.users user " +
        // "where user.login = ?#{authentication.name}"
        // )
        // Page<Assignment> findAllWithToOneRelationshipsByUser(Pageable pageable);

        // @Query(
        // "select assignment from Assignment assignment " +
        // "left join assignment.activity " +
        // "left join assignment.orgUnit " +
        // "left join assignment.team " +
        // "join assignment.team.users user " +
        // "where assignment.id =:id and user.login = ?#{authentication.name}"
        // )
        // Optional<Assignment> findOneWithToOneRelationshipsByUser(@Param("id") Long
        // id);
        //
        // @Query(
        // "select assignment from Assignment assignment " +
        // "left join assignment.activity " +
        // "left join assignment.orgUnit " +
        // "left join assignment.team " +
        // "join assignment.team.users user " +
        // "where assignment.uid =:uid and user.login = ?#{authentication.name}"
        // )
        // Optional<Assignment> findOneWithToOneRelationshipsByUser(@Param("uid") String
        // uid);
}

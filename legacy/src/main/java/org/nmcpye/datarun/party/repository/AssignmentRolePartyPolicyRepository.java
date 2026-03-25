package org.nmcpye.datarun.party.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/// Spring Data JPA repository for the AssignmentRolePartyPolicy entity.
///
/// @author Hamza Assada 28/12/2025
@Repository
public interface AssignmentRolePartyPolicyRepository extends BaseJpaRepository<AssignmentRolePartyPolicy, String> {
    //
    // @Query("""
    // SELECT b FROM AssignmentRolePartyPolicy b
    // WHERE b.assignmentId = :assignmentId
    // AND b.name = :roleName
    // AND (b.vocabularyId = :vocabularyId OR b.vocabularyId IS NULL)
    // ORDER BY b.vocabularyId NULLS LAST
    // """)
    // List<AssignmentRolePartyPolicy> findBindingsForResolution(
    // @Param("assignmentId") String assignmentId,
    // @Param("roleName") String roleName,
    // @Param("vocabularyId") String vocabularyId
    // );

    List<AssignmentRolePartyPolicy> findByAssignmentIdIn(Collection<String> assignmentIds);

    List<AssignmentRolePartyPolicy> findByAssignmentIdAndRole(String assignmentId, String role);

    List<AssignmentRolePartyPolicy> findByAssignmentIdAndRoleIn(String assignmentId, Collection<String> roles);

    Collection<AssignmentRolePartyPolicy> findByAssignmentId(String assignmentId);

    Optional<AssignmentRolePartyPolicy> findByUid(String uid);

    List<AssignmentRolePartyPolicy> findByAssignmentIdOrAssignmentUid(String assignmentId, String assignmentUid);

    /**
     * Checks if any AssignmentRolePartyPolicy entities exist that reference the
     * given
     * partySetId.
     *
     * @param partySetId The ID of the PartySet to check for.
     * @return true if at least one binding exists, false otherwise.
     */
    boolean existsByPartySetId(String partySetId);

    boolean existsByPartySetUid(String partySetUid);
}

package org.nmcpye.datarun.party.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;

import org.nmcpye.datarun.party.entities.AssignmentRoleDataPolicy;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/// Spring Data JPA repository for the AssignmentRolePartyPolicy entity.
///
/// @author Hamza Assada 28/12/2025
@Repository
public interface AssignmentRoleDataPolicyRepository
    extends BaseJpaRepository<AssignmentRoleDataPolicy, String> {

  List<AssignmentRoleDataPolicy> findByAssignmentIdIn(Collection<String> assignmentIds);

  Collection<AssignmentRoleDataPolicy> findByAssignmentId(String assignmentId);

  List<AssignmentRoleDataPolicy> findByAssignmentIdAndRole(String assignmentId, String role);

  List<AssignmentRoleDataPolicy> findByAssignmentIdAndRoleIn(String assignmentId, Collection<String> roles);
}

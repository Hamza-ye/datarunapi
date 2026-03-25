package org.nmcpye.datarun.jpa.accessfilter.projector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.jpa.accessfilter.entity.UserExecutionContext;
import org.nmcpye.datarun.jpa.accessfilter.repository.UserExecutionContextRepository;
import org.nmcpye.datarun.iam.security.CurrentUserInfoService;
import org.nmcpye.datarun.sharedkernal.enumeration.AccessLevel;
import org.nmcpye.datarun.sharedkernal.enumeration.FormPermission;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.nmcpye.datarun.jpa.accessfilter.event.UserAccessRulesChangedEvent;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.nmcpye.datarun.party.entities.AssignmentRoleDataPolicy;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.nmcpye.datarun.party.repository.AssignmentMemberRepository;
import org.nmcpye.datarun.party.repository.AssignmentRoleDataPolicyRepository;
import org.nmcpye.datarun.party.repository.AssignmentRolePartyPolicyRepository;
import org.nmcpye.datarun.party.resolution.engine.PartyResolutionEngine;
import org.springframework.context.event.EventListener;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Background projector service that handles Phase 1 of the CQRS execution
 * boundary.
 * It flattens the complex planning rules (Teams, OrgUnits, Assignments) into
 * the
 * fast UserExecutionContext table for the API and Mobile clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserExecutionContextProjector {

        private final UserExecutionContextRepository executionContextRepository;
        private final CurrentUserInfoService currentUserInfoService;
        private final AssignmentMemberRepository assignmentMemberRepository;
        private final AssignmentRepository assignmentRepository;
        private final AssignmentRoleDataPolicyRepository assignmentRoleDataPolicyRepository;
        private final AssignmentRolePartyPolicyRepository assignmentRolePartyPolicyRepository;
        private final PartyResolutionEngine partyResolutionEngine;
        private final ObjectMapper objectMapper;

        /**
         * Rebuilds the flattened execution context for a single user.
         * This method translates the scattered runtime arrays into a persistent CQRS
         * view.
         */
        @Async
        @EventListener
        @Transactional
        public void rebuildForUser(UserAccessRulesChangedEvent event) {
                String userLogin = event.getUserLogin();
                log.info("Rebuilding CQRS User Execution Context for user: {}", userLogin);

                // 1. Fetch current info
                var teamInfo = currentUserInfoService.getUserTeamInfo(userLogin);
                String userUid = teamInfo.getUserUID();

                var activityInfo = currentUserInfoService.getUserActivityInfo(userLogin);
                var groupInfo = currentUserInfoService.getUserGroupIds(userLogin);
                var formAccesses = currentUserInfoService.getUserFormAccess(userLogin, teamInfo.getTeamUIDs());

                // We use a map to deduplicate and resolve the Highest Permission per Entity
                // Key: "ENTITY_TYPE:ENTITY_UID", Value: UserExecutionContext
                Map<String, UserExecutionContext> contextMap = new HashMap<>();

                teamInfo.getTeamUIDs()
                                .forEach(uid -> appendOrUpgrade(contextMap, userUid, "TEAM", uid, AccessLevel.READ,
                                                Map.<String, Object>of("source", "user_team", "uid", uid)));
                teamInfo.getManagedTeamUIDs()
                                .forEach(uid -> appendOrUpgrade(contextMap, userUid, "TEAM", uid, AccessLevel.ALL,
                                                Map.<String, Object>of("source", "managed_team", "uid", uid)));

                activityInfo.getActivityUIDs()
                                .forEach(uid -> appendOrUpgrade(contextMap, userUid, "ACTIVITY", uid, AccessLevel.READ,
                                                Map.<String, Object>of("source", "user_activity", "uid", uid)));

                groupInfo.getUserGroupUIDs()
                                .forEach(uid -> appendOrUpgrade(contextMap, userUid, "USER_GROUP", uid,
                                                AccessLevel.READ,
                                                Map.<String, Object>of("source", "user_group", "uid", uid)));

                // 5. Flatten Data Templates (Forms)
                for (UserFormAccess formAccess : formAccesses) {
                        String formUid = formAccess.getForm();
                        AccessLevel highestFormLevel = AccessLevel.READ;

                        for (FormPermission perm : formAccess.getPermissions()) {
                                AccessLevel mapped = mapFormPermission(perm);
                                if (mapped.ordinal() > highestFormLevel.ordinal()) {
                                        highestFormLevel = mapped;
                                }
                        }
                        appendOrUpgrade(contextMap, userUid, "DATA_TEMPLATE", formUid, highestFormLevel,
                                        Map.<String, Object>of("source", "user_form_access", "uid", formUid));
                }

                // 6. Process Assignments (Legacy + New Bindings)
                Set<String> principalIds = new HashSet<>();
                principalIds.add(userUid);
                principalIds.addAll(teamInfo.getTeamUIDs());
                principalIds.addAll(groupInfo.getUserGroupUIDs());

                List<String> activeAssignmentIds = assignmentMemberRepository
                                .findActiveAssignmentIdsForPrincipalsAndTeams(
                                                principalIds, teamInfo.getTeamUIDs());

                if (!activeAssignmentIds.isEmpty()) {
                        List<Assignment> assignments = assignmentRepository.findAllById(activeAssignmentIds);

                        List<AssignmentRoleDataPolicy> dataPolicies = assignmentRoleDataPolicyRepository
                                        .findByAssignmentIdIn(activeAssignmentIds);
                        Map<String, List<AssignmentRoleDataPolicy>> dataPoliciesByAssignment = dataPolicies.stream()
                                        .collect(Collectors.groupingBy(p -> String.valueOf(p.getAssignment().getId())));

                        List<AssignmentRolePartyPolicy> partyPolicies = assignmentRolePartyPolicyRepository
                                        .findByAssignmentIdIn(activeAssignmentIds);
                        Map<String, List<AssignmentRolePartyPolicy>> partyPoliciesByAssignment = partyPolicies.stream()
                                        .collect(Collectors.groupingBy(b -> String.valueOf(b.getAssignment().getId())));

                        // Fetch explicit members for role lookup
                        List<AssignmentMember> members = assignmentMemberRepository
                                        .findActiveAssignmentIdsForPrincipals(activeAssignmentIds, principalIds);
                        Map<String, List<AssignmentMember>> membersByAssignment = members.stream()
                                        .collect(Collectors.groupingBy(m -> String.valueOf(m.getAssignmentId())));

                        for (Assignment assignment : assignments) {
                                String assignmentIdStr = String.valueOf(assignment.getId());

                                // Legacy Path (if still present)
                                if (assignment.getTeam() != null) {
                                        appendOrUpgrade(contextMap, userUid, "TEAM", assignment.getTeam().getUid(),
                                                        AccessLevel.READ,
                                                        Map.<String, Object>of("assignment", assignmentIdStr,
                                                                        "legacy_team",
                                                                        assignment.getTeam().getUid()));
                                }
                                if (assignment.getOrgUnit() != null) {
                                        appendOrUpgrade(contextMap, userUid, "ORG_UNIT",
                                                        assignment.getOrgUnit().getUid(),
                                                        AccessLevel.READ,
                                                        Map.<String, Object>of("assignment", assignmentIdStr,
                                                                        "legacy_org_unit",
                                                                        assignment.getOrgUnit().getUid()));
                                }

                                // New Bindings Path
                                List<AssignmentRoleDataPolicy> assignmentDataPolicies = dataPoliciesByAssignment
                                                .getOrDefault(
                                                                assignmentIdStr,
                                                                List.of());
                                List<AssignmentRolePartyPolicy> assignmentPartyPolicies = partyPoliciesByAssignment
                                                .getOrDefault(
                                                                assignmentIdStr,
                                                                List.of());

                                Set<String> userRolesInAssignment = membersByAssignment
                                                .getOrDefault(assignmentIdStr, List.of())
                                                .stream()
                                                .map(AssignmentMember::getRole)
                                                .collect(Collectors.toSet());

                                // If the user has an implicit assignment (through team without member row), we
                                // assume a generic role.
                                // Assuming "MEMBER" or "DEFAULT" applies for any general member policy if
                                // needed, or we just rely on wildcard policies.
                                userRolesInAssignment.add("MEMBER");

                                for (AssignmentRoleDataPolicy policy : assignmentDataPolicies) {
                                        boolean appliesToUser = userRolesInAssignment.contains(policy.getRole())
                                                        || "ANY".equalsIgnoreCase(policy.getRole());

                                        if (!appliesToUser) {
                                                continue;
                                        }

                                        Map<String, Object> provenanceEntry = new HashMap<>();
                                        provenanceEntry.put("source", "data_policy");
                                        provenanceEntry.put("assignment", assignmentIdStr);
                                        provenanceEntry.put("role", policy.getRole());
                                        provenanceEntry.put("policy", policy.getUid());

                                        appendOrUpgrade(contextMap, userUid, "DATA_TEMPLATE",
                                                        policy.getDataTemplate().getUid(),
                                                        AccessLevel.valueOf(policy.getAccessLevel().name()),
                                                        provenanceEntry);
                                }

                                for (AssignmentRolePartyPolicy binding : assignmentPartyPolicies) {
                                        boolean appliesToUser = userRolesInAssignment.contains(binding.getRole())
                                                        || "ANY".equalsIgnoreCase(binding.getRole());

                                        if (!appliesToUser) {
                                                continue;
                                        }

                                        PartyResolutionRequest resolutionRequest = PartyResolutionRequest.builder()
                                                        .assignmentId(assignmentIdStr)
                                                        .userId(userUid)
                                                        .limit(1000000)
                                                        .offset(0)
                                                        .build();

                                        String specJson = null;
                                        try {
                                                specJson = objectMapper
                                                                .writeValueAsString(binding.getPartySet().getSpec());
                                        } catch (Exception e) {
                                                log.error("Failed to serialize PartySetSpec", e);
                                        }

                                        // Bypass security (isMaterialized = false) because we are currently BUILDING
                                        // the security context
                                        List<ResolvedParty> resolvedParties = partyResolutionEngine.executeStrategy(
                                                        binding.getPartySet().getKind(),
                                                        binding.getPartySet().getId(),
                                                        specJson,
                                                        false,
                                                        resolutionRequest);
                                        Map<String, Object> provenanceEntry = new HashMap<>();
                                        provenanceEntry.put("source", "party_policy");
                                        provenanceEntry.put("assignment", assignmentIdStr);
                                        provenanceEntry.put("role", binding.getRole());
                                        provenanceEntry.put("policy", binding.getUid());

                                        for (ResolvedParty rp : resolvedParties) {
                                                appendOrUpgrade(contextMap, userUid, rp.getSource(), rp.getUid(),
                                                                AccessLevel.READ,
                                                                provenanceEntry);
                                        }

                                        if (binding.getVocabulary() != null) {
                                                appendOrUpgrade(contextMap, userUid, "DATA_TEMPLATE",
                                                                binding.getVocabulary().getUid(),
                                                                AccessLevel.READ, provenanceEntry);
                                        }
                                }
                        }
                }

                // 7. Delete old context and insert new context
                executionContextRepository.deleteByUserUid(userUid);
                executionContextRepository.persistAllAndFlush(contextMap.values());
                log.debug("Successfully rebuilt {} context rows for user {}", contextMap.size(), userUid);
        }

        private void appendOrUpgrade(Map<String, UserExecutionContext> map, String userUid, String type,
                        String entityUid,
                        AccessLevel level, Map<String, Object> provenanceEntry) {
                String key = type + ":" + entityUid;
                UserExecutionContext existing = map.get(key);

                if (existing == null) {
                        Map<String, Object> newProvenance = new HashMap<>();
                        if (provenanceEntry != null && !provenanceEntry.isEmpty()) {
                                String entryKey = provenanceEntry.getOrDefault("source", "unknown") + "_"
                                                + UUID.randomUUID().toString().substring(0, 8);
                                newProvenance.put(entryKey, provenanceEntry);
                        }

                        map.put(key, UserExecutionContext.builder()
                                        .userUid(userUid)
                                        .entityType(type)
                                        .entityUid(entityUid)
                                        .resolvedPermission(level)
                                        .provenance(newProvenance)
                                        .build());
                } else {
                        // Upgrade permission if the new one is higher
                        if (level.ordinal() > existing.getResolvedPermission().ordinal()) {
                                existing.setResolvedPermission(level);
                        }
                        if (provenanceEntry != null && !provenanceEntry.isEmpty()) {
                                if (existing.getProvenance() == null) {
                                        existing.setProvenance(new HashMap<>());
                                }
                                String entryKey = provenanceEntry.getOrDefault("source", "unknown") + "_"
                                                + UUID.randomUUID().toString().substring(0, 8);
                                existing.getProvenance().put(entryKey, provenanceEntry);
                        }
                }
        }

        private AccessLevel mapFormPermission(FormPermission permission) {
                return switch (permission) {
                        case DELETE_SUBMISSIONS, DELETE_SUBMISSIONS_FROM_USERS -> AccessLevel.DELETE;
                        case ADD_SUBMISSIONS, EDIT_SUBMISSIONS, EDIT_SUBMISSIONS_FROM_USERS, APPROVE_SUBMISSIONS ->
                                AccessLevel.UPDATE;
                        default -> AccessLevel.READ; // VIEW_SUBMISSIONS, VIEW_SUBMISSIONS_FROM_USERS
                };
        }
}

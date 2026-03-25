package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.nmcpye.datarun.party.dto.AssignmentManifestDto;
import org.nmcpye.datarun.party.dto.AssignmentManifestProjection;
import org.nmcpye.datarun.party.dto.AssignmentStatus;
import org.nmcpye.datarun.party.dto.PagedRequest;
import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.nmcpye.datarun.party.repository.AssignmentMemberRepository;
import org.nmcpye.datarun.party.repository.AssignmentRoleDataPolicyJooqRepository;
import org.nmcpye.datarun.party.repository.AssignmentRolePartyPolicyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManifestService {

        private final AssignmentRepository assignmentRepo;
        private final AssignmentMemberRepository memberRepo;
        /**
         * vocabRepo
         */
        private final DataTemplateRepository templateRepository;
        private final AssignmentRolePartyPolicyRepository bindingRepo;
        private final AssignmentRoleDataPolicyJooqRepository assignmentRoleDataPolicyRepo;

        // In a real app, you'd inject a "UserContext" to get the current user's ID
        // You would inject a service to get user's teams/groups, or resolve them here.
        // For now, we'll assume they are passed in.
        @Transactional(readOnly = true)
        public Page<AssignmentManifestDto> buildManifest(String userId, Collection<String> teamIds,
                        Collection<String> userGroupIds, PagedRequest pagedRequest) {

                // 1. Gather all principals for the user
                Set<String> principalIds = new HashSet<>(teamIds);
                principalIds.add(userId);
                principalIds.addAll(userGroupIds);

                // 2. Find all active Assignment IDs in a single query
                Page<String> assignmentIds = (pagedRequest.getSince() == null)
                                ? memberRepo
                                                .findActiveAssignmentIdsForPrincipalsAndTeams(principalIds, teamIds,
                                                                pagedRequest.getPageable())
                                : memberRepo
                                                .findActiveAssignmentIdsForPrincipalsAndTeams(principalIds, teamIds,
                                                                pagedRequest.getSince(),
                                                                pagedRequest.getPageable());
                if (assignmentIds.isEmpty()) {
                        return Page.empty(pagedRequest.getPageable());
                }

                // 3. Fetch all required data in bulk to avoid N+1 queries
                List<AssignmentManifestProjection> assignments = assignmentRepo
                                .findAssignmentManifestsByUids(assignmentIds.getContent());

                List<AssignmentRolePartyPolicy> bindings = bindingRepo.findByAssignmentIdIn(assignmentIds.getContent());

                // Collect all unique vocabulary/template IDs from the bindings
                Set<String> vocabularyIds = bindings.stream()
                                .map(AssignmentRolePartyPolicy::getVocabulary)
                                .map(DataTemplate::getId)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet());

                Map<String, DataTemplate> templatesById = templateRepository.findAllById(vocabularyIds).stream()
                                .collect(Collectors.toMap(DataTemplate::getId, Function.identity()));

                // Group bindings by assignment for efficient mapping
                Map<String, List<AssignmentRolePartyPolicy>> bindingsByAssignmentId = bindings.stream()
                                .collect(Collectors.groupingBy(b -> b.getAssignment().getId()));

                // Group bindings by assignment for efficient mapping
                Map<String, List<AssignmentMember>> principalAssignmentRolesByAssignmentId = memberRepo
                                .findActiveAssignmentIdsForPrincipals(assignmentIds.getContent(), principalIds).stream()
                                .collect(Collectors.groupingBy(AssignmentMember::getAssignmentId));

                // 4. Build the final DTOs in memory
                final var bindingsManifest = assignments.stream().map(assign -> {
                        // base forms declared on the assignment
                        Set<String> assignmentFormUids = Optional.ofNullable(assign.getForms())
                                        .orElse(Collections.emptySet());

                        // compute user roles for this assignment from assignment_member rows
                        List<AssignmentMember> amRows = principalAssignmentRolesByAssignmentId
                                        .get(assign.getAssignmentId());
                        Set<String> userRoles = amRows.stream()
                                        .filter(am -> principalIds.contains(am.getMemberId())
                                                        || userId.equals(am.getMemberId()))
                                        .map(AssignmentMember::getRole)
                                        .filter(java.util.Objects::nonNull)
                                        .collect(Collectors.toSet());

                        // fetch allowed templates (data_template.uids) for this user/principals in this
                        // assignment
                        List<String> allowedTemplateUids = assignmentRoleDataPolicyRepo.findAllowedTemplateUids(
                                        assign.getAssignmentId(), userId, principalIds, userRoles);

                        // intersect with assignment declared forms to keep scope
                        Set<String> vocabUids = assignmentFormUids.stream()
                                        .filter(allowedTemplateUids::contains)
                                        .collect(Collectors.toSet());

                        List<AssignmentRolePartyPolicy> assignBindings = bindingsByAssignmentId.getOrDefault(
                                        assign.getAssignmentId(),
                                        Collections.emptyList());

                        return AssignmentManifestDto.builder()
                                        .assignmentUid(assign.getAssignmentUid())
                                        .label(assign.getLabel()) // Or other meaningful label
                                        .status(AssignmentStatus.getAssignmentStatus(assign.getStatus()))
                                        .allowedTemplateUids(vocabUids)
                                        .bindings(mapBindings(assignBindings, templatesById))
                                        // legacy
                                        .forms(assign.getForms())
                                        .orgUnitUid(assign.getOrgUnitUid())
                                        .activityUid(assign.getActivityUid())
                                        .teamUid(assign.getTeamUid())
                                        .deleted(assign.getDeleted())
                                        .startDay(assign.getStartDay())
                                        .build();
                }).toList();

                return new PageImpl<>(bindingsManifest, assignmentIds.getPageable(), bindingsManifest.size());
        }

        private List<AssignmentManifestDto.BindingDto> mapBindings(List<AssignmentRolePartyPolicy> source,
                        Map<String, DataTemplate> templatesById) {
                return source.stream().map(b -> {
                        DataTemplate template = (b.getVocabulary() != null)
                                        ? templatesById.get(b.getVocabulary().getId())
                                        : null;
                        return AssignmentManifestDto.BindingDto.builder()
                                        .roleName(b.getName())
                                        .templateUid(template != null ? template.getUid() : null) // Null for
                                                                                                  // global-assignment
                                                                                                  // roles
                                        .partySetId(b.getPartySet().getId())
                                        .combineMode(b.getCombineMode())
                                        .provenance("Role Binding")
                                        .build();
                }).toList();
        }
}

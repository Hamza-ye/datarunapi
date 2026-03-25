package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;

import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.nmcpye.datarun.party.dto.AssignmentRolePartyPolicyDto;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.nmcpye.datarun.party.entities.PartySet;
import org.nmcpye.datarun.party.exceptions.InvalidBindingException;
import org.nmcpye.datarun.party.exceptions.NotFoundObjectException;
import org.nmcpye.datarun.party.repository.PartySetRepository;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BindingValidationService {

    private final PartySetRepository partySetRepository;
    private final AssignmentRepository assignmentRepository;
    private final DataTemplateRepository dataTemplateRepository;

    public AssignmentRolePartyPolicy validatedBinding(AssignmentRolePartyPolicyDto dto) {
        if (dto.getPartySetUid() == null) {
            throw new InvalidBindingException("PartySet ID cannot be null.");
        }
        if (dto.getAssignmentUid() == null || dto.getAssignmentUid().isBlank()) {
            throw new InvalidBindingException("Assignment UID cannot be null.");
        }

        final var binding = AssignmentRolePartyPolicy.builder()
                .assignment(validatedBindingAssignment(dto).persisted())
                .partySet(validatedBindingPartySet(dto).persisted())
                .vocabulary(validatedBindingVocabulary(dto).persisted())
                .role(dto.getRole())
                .combineMode(dto.getCombineMode())
                .name(dto.getName())
                .build();

        binding.setUid(dto.getUid() != null ? dto.getUid() : CodeGenerator.generateUid());
        if (dto.getId() != null) {
            binding.setId(dto.getId());
        }

        return binding;
    }

    public Assignment validatedBindingAssignment(AssignmentRolePartyPolicyDto dto) {
        return assignmentRepository.findByUid(dto.getAssignmentUid())
                .orElseThrow(() -> new NotFoundObjectException(
                        "Assignment with UID " + dto.getAssignmentUid() + " does not exist."));
    }

    public PartySet validatedBindingPartySet(AssignmentRolePartyPolicyDto dto) {
        return partySetRepository.findByUid(dto.getAssignmentUid())
                .orElseThrow(() -> new NotFoundObjectException(
                        "PartySet with UID " + dto.getPartySetUid() + " does not exist."));
    }

    public DataTemplate validatedBindingVocabulary(AssignmentRolePartyPolicyDto dto) {
        return dataTemplateRepository.findByUid(dto.getAssignmentUid()).orElse(null);
    }

}

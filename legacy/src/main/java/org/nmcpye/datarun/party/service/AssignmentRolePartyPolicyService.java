package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.datacapture.datasubmission.events.EventChangeType;
import org.nmcpye.datarun.party.dto.AssignmentRolePartyPolicyDto;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.nmcpye.datarun.party.events.AssignmentBindingChangedEvent;
import org.nmcpye.datarun.party.mapper.AssignmentRolePartyPolicyMapper;
import org.nmcpye.datarun.party.repository.AssignmentRolePartyPolicyRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AssignmentRolePartyPolicyService {

    private final AssignmentRolePartyPolicyRepository bindingRepository;
    private final AssignmentRolePartyPolicyMapper bindingMapper;
    private final ApplicationEventPublisher eventPublisher;

    public AssignmentRolePartyPolicyDto save(AssignmentRolePartyPolicyDto dto) {
        AssignmentRolePartyPolicy entity = bindingMapper.toEntity(dto);
        // Add logic for generating UID if not present
        entity = bindingRepository.persist(entity);

        // **IMPORTANT**: Publish an event so permissions can be recalculated
        eventPublisher.publishEvent(new AssignmentBindingChangedEvent(entity, EventChangeType.CREATE));

        return bindingMapper.toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<AssignmentRolePartyPolicyDto> findByAssignmentId(String assignmentId) {
        return bindingRepository.findByAssignmentIdOrAssignmentUid(assignmentId, assignmentId).stream()
                .map(bindingMapper::toDto)
                .collect(Collectors.toList());
    }

    public void delete(String id) {
        boolean isUid = id != null && id.length() == 11;
        (!isUid ? bindingRepository.findById(id) : bindingRepository.findByUid(id))
                .ifPresent(binding -> {
                    bindingRepository.delete(binding);
                    // **IMPORTANT**: Publish an event for deletion as well
                    eventPublisher.publishEvent(new AssignmentBindingChangedEvent(binding, EventChangeType.DELETE));
                });
    }
}

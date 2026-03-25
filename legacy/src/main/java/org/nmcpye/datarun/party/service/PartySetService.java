package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.exception.InvalidRequestException;
import org.nmcpye.datarun.party.dto.PagedRequest;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.PartySetDto;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySet;
import org.nmcpye.datarun.party.exceptions.DeletionConflictException;
import org.nmcpye.datarun.party.exceptions.NotFoundObjectException;
import org.nmcpye.datarun.party.mapper.PartySetMapper;
import org.nmcpye.datarun.party.repository.AssignmentRolePartyPolicyRepository;
import org.nmcpye.datarun.party.repository.PartySetRepository;
import org.nmcpye.datarun.party.resolution.engine.PartyResolutionEngine;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PartySetService {

    private final PartySetRepository partySetRepository;
    private final PartyResolutionEngine engine;
    private final PartySetMapper partySetMapper;
    private final AssignmentRolePartyPolicyRepository bindingRepository;

    public PartySetDto save(PartySetDto partySetDto) {
        PartySet partySet = partySetMapper.toEntity(partySetDto);

        partySet = partySetRepository.persistAndFlush(partySet);
        return partySetMapper.toDto(partySet);
    }

    @Transactional(readOnly = true)
    public List<PartySetDto> findAll() {
        return partySetRepository.findAll().stream()
                .map(partySetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PartySetDto> findOne(String id) {
        return partySetRepository.findById(id).map(partySetMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ResolvedParty> findPartiesBySetId(String optionSetId, PagedRequest pagedRequest,
            CurrentUserDetails user) {
        final var pageable = pagedRequest.getPageable();
        final var partySet = partySetRepository.findByUid(optionSetId)
                .orElseThrow(
                        () -> new NotFoundObjectException("PartySet with optionSetId: " + optionSetId + " not found"));

        final List<ResolvedParty> parties;
        try {
            parties = engine.executeStrategy(partySet.getKind(), partySet.getId(),
                    partySet.getSpec() != null ? PagedRequest.specToString(partySet.getSpec()) : null, true,
                    PartyResolutionRequest.builder()
                            .userId(user.getId())
                            .limit(pagedRequest.getSize())
                            .offset(pagedRequest.getPage() * pagedRequest.getSize())
                            .since(pagedRequest.getSince())
                            .build());
        } catch (IOException e) {
            throw new InvalidRequestException("invalid PartySet specs:\n" + e.getMessage());
        }

        return new PageImpl<>(parties, pageable, parties.size());
    }

    public void delete(String id) {
        boolean isUid = id != null && id.length() == 11;
        // **VALIDATION STEP**
        // Check if any bindings are currently using this PartySet.
        if (bindingRepository.existsByPartySetUid(id) || (!isUid &&
                bindingRepository.existsByPartySetId(id))) {
            throw new DeletionConflictException(
                    "Cannot delete PartySet with ID " + id + ". It is currently in use by one or more bindings.");
        }

        // If the check passes, proceed with the deletion.
        if (!isUid) {
            partySetRepository.deleteById(id);
        } else {
            partySetRepository.deleteByUid(id);
        }
    }
}

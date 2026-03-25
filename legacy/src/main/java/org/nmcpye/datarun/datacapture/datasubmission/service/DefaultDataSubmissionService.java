package org.nmcpye.datarun.datacapture.datasubmission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.nmcpye.datarun.jpa.accessfilter.UserAccessService;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.events.EventChangeType;
import org.nmcpye.datarun.datacapture.datasubmission.events.SubmissionSavedEvent;
import org.nmcpye.datarun.datacapture.datasubmission.repository.DataSubmissionRepository;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.DefaultJpaSoftDeleteService;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.JpaSoftDeleteObject;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.nmcpye.datarun.outbox.repository.OutboxWritePort;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link DataSubmission}.
 */
@Service
@Primary
public class DefaultDataSubmissionService
        extends DefaultJpaSoftDeleteService<DataSubmission>
        implements DataSubmissionService {
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OutboxWritePort outboxRepo;

    public DefaultDataSubmissionService(
            DataSubmissionRepository repository,
            CacheManager cacheManager,
            UserAccessService userAccessService,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper, OutboxWritePort outboxRepo) {
        super(repository, cacheManager, userAccessService, eventPublisher);
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.outboxRepo = outboxRepo;
    }

    @Transactional
    @Override
    public DataSubmission upsert(DataSubmission entity, CurrentUserDetails user, EntitySaveSummaryVM summary) {
        List<DataSubmission> results = upsertAll(List.of(entity), user, summary);

        // This check is a safeguard for unexpected behavior from upsertAll,
        // rather than input validation.
        if (results.isEmpty()) {
            throw new IllegalStateException(
                    "UpsertAll returned an empty list when processing a single entity. This indicates an internal logic error.");
        }
        return results.get(0);
    }

    private boolean updateEntityFields(DataSubmission existingEntity, DataSubmission incomingEntity) {
        if (Boolean.TRUE.equals(existingEntity.getDeleted()) && Boolean.TRUE.equals(incomingEntity.getDeleted())) {
            return true;
        }
        if (Boolean.TRUE.equals(existingEntity.getDeleted()) && Boolean.FALSE.equals(incomingEntity.getDeleted())) {
            existingEntity.setDeleted(false);
            existingEntity.setDeletedAt(null);
            return false;
        }

        if (Boolean.TRUE.equals(incomingEntity.getDeleted()) && Boolean.FALSE.equals(existingEntity.getDeleted())) {
            existingEntity.setDeleted(true);
            existingEntity.setDeletedAt(Instant.now());
            return true;
        }
        existingEntity.setDeleted(incomingEntity.getDeleted());
        existingEntity.setDeletedAt(!incomingEntity.getDeleted() && existingEntity.getDeleted() ? Instant.now()
                : existingEntity.getDeletedAt());

        existingEntity.setFormData(incomingEntity.getFormData().deepCopy());
        existingEntity.setActivity(incomingEntity.getActivity());
        existingEntity.setAssignment(incomingEntity.getAssignment());
        existingEntity.setTeam(incomingEntity.getTeam());
        existingEntity.setOrgUnit(incomingEntity.getOrgUnit());
        existingEntity.setStatus(incomingEntity.getStatus());
        existingEntity.setOrgUnitCode(incomingEntity.getOrgUnitCode());
        existingEntity.setOrgUnitName(incomingEntity.getOrgUnitName());
        existingEntity.setTeamCode(incomingEntity.getTeamCode());

        return false;
    }

    @Transactional
    @Override
    public List<DataSubmission> upsertAll(Collection<DataSubmission> entities,
            CurrentUserDetails user, EntitySaveSummaryVM summary) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        Set<String> incomingUids = entities.stream()
                .map(entity -> {
                    if (entity.getUid() == null) {
                        summary.getFailed().put("NULL UID",
                                "Entity in bulk operation must have a UID for upsert operation.");
                        throw new IllegalArgumentException(
                                "Entity in bulk operation must have a UID for upsert operation.");
                    }
                    return entity.getUid();
                })
                .collect(Collectors.toSet());

        List<DataSubmission> existingEntitiesFromDb = jpaAuditableObjectRepository
                .findAllByUidIn(new ArrayList<>(incomingUids));
        Map<String, DataSubmission> existingEntitiesMap = existingEntitiesFromDb.stream()
                .collect(Collectors.toMap(JpaSoftDeleteObject::getUid, Function.identity()));

        List<DataSubmission> entitiesToPersist = new ArrayList<>();
        List<DataSubmission> entitiesToUpdate = new ArrayList<>();
        List<DataSubmission> entitiesToDelete = new ArrayList<>();

        for (DataSubmission incomingEntity : entities) {
            DataSubmission existingEntity = existingEntitiesMap.get(incomingEntity.getUid());
            boolean isNew = (existingEntity == null);

            if (isNew) {
                if (incomingEntity.getId() == null) {
                    incomingEntity.setId(CodeGenerator.nextUlid());
                }
                entitiesToPersist.add(incomingEntity);
            } else {
                final var deleted = updateEntityFields(existingEntity, incomingEntity);
                if (deleted)
                    entitiesToDelete.add(existingEntity);
                else
                    entitiesToUpdate.add(existingEntity);
            }
        }

        List<DataSubmission> persistedResults = List.of();
        List<DataSubmission> updatedResults = List.of();
        List<DataSubmission> deletedResults = List.of();

        if (!entitiesToPersist.isEmpty()) {
            persistedResults = jpaAuditableObjectRepository.persistAllAndFlush(entitiesToPersist);
            final var outboxEvents = persistedResults.stream()
                    .map(this::enqueueSubmissionsOutbox)
                    .toList();
            outboxRepo.insertByEventType(outboxEvents, "SAVE");

            persistedResults.forEach(s -> eventPublisher.publishEvent(new SubmissionSavedEvent(s.getId(),
                    EventChangeType.CREATE, s.getLockVersion())));
            summary.getCreated().addAll(persistedResults.stream().map(DataSubmission::getUid).toList());
        }
        if (!entitiesToUpdate.isEmpty()) {
            updatedResults = jpaAuditableObjectRepository.updateAllAndFlush(entitiesToUpdate);
            final var outboxEvents = updatedResults.stream()
                    .map(this::enqueueSubmissionsOutbox)
                    .toList();
            outboxRepo.insertByEventType(outboxEvents, "UPDATE");

            updatedResults.forEach(s -> eventPublisher.publishEvent(new SubmissionSavedEvent(s.getId(),
                    EventChangeType.UPDATE, s.getLockVersion()))); // Apply post-update hook for each
            summary.getUpdated().addAll(updatedResults.stream().map(DataSubmission::getUid).toList());
        }

        if (!entitiesToDelete.isEmpty()) {
            deletedResults = jpaAuditableObjectRepository.updateAllAndFlush(entitiesToDelete);
            final var outboxEvents = deletedResults.stream()
                    .map(this::enqueueSubmissionsOutbox)
                    .toList();
            outboxRepo.insertByEventType(outboxEvents, "DELETE");

            deletedResults.forEach(s -> eventPublisher.publishEvent(new SubmissionSavedEvent(s.getId(),
                    EventChangeType.DELETE, s.getLockVersion()))); // Apply post-update hook for each
            summary.getUpdated().addAll(deletedResults.stream().map(DataSubmission::getUid).toList());
        }

        List<DataSubmission> combinedResults = new ArrayList<>(persistedResults);
        combinedResults.addAll(updatedResults);
        combinedResults.addAll(deletedResults);
        return combinedResults;
    }

    @Transactional
    @Override
    public void softDelete(DataSubmission object) {
        var outbox = enqueueSubmissionsOutbox(object);
        outboxRepo.insertByEventType(List.of(outbox), "DELETE");
        super.softDelete(object);
        eventPublisher.publishEvent(new SubmissionSavedEvent(object.getId(),
                EventChangeType.DELETE, object.getLockVersion()));
    }

    private OutboxWritePort.OutboxInsert enqueueSubmissionsOutbox(DataSubmission s) {
        String payload;
        try {
            Map<String, Object> fatEvent = new HashMap<>();
            fatEvent.put("aggregate_id", s.getUid());
            fatEvent.put("aggregate_type", "Submission");
            fatEvent.put("correlation_id", s.getId());
            fatEvent.put("occurred_at",
                    s.getFinishedEntryTime() != null ? s.getFinishedEntryTime().toString() : Instant.now().toString());
            fatEvent.put("recorded_at", Instant.now().toString());
            fatEvent.put("template_uid", s.getForm());
            fatEvent.put("org_unit_uid", s.getOrgUnit());
            fatEvent.put("team_uid", s.getTeam());
            fatEvent.put("data", s.getFormData());

            payload = objectMapper.writeValueAsString(fatEvent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to stringify fat event payload", e);
        }

        return new OutboxWritePort.OutboxInsert(
                s.getId(),
                s.getUid(),
                s.getFormVersion(),
                payload,
                Instant.now(),
                s.getSerialNumber(),
                s.getId(), // correlation_id
                s.getFinishedEntryTime() != null ? s.getFinishedEntryTime() : Instant.now() // occurred_at
        );
    }
}

package org.nmcpye.datarun.web.common;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.*;
import org.nmcpye.datarun.sharedkernal.repository.CreateAccessDeniedException;
import org.nmcpye.datarun.sharedkernal.repository.DeleteAccessDeniedException;
import org.nmcpye.datarun.sharedkernal.repository.UpdateAccessDeniedException;
import org.nmcpye.datarun.web.mvc.annotation.ApiVersion;
import org.nmcpye.datarun.web.errors.BadRequestAlertException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.nmcpye.datarun.web.rest.util.HeaderUtil;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@ApiVersion({DRunApiVersion.DEFAULT, DRunApiVersion.ALL})
@Slf4j
public abstract class BaseReadWriteResource<T extends IdentifiableObject<ID>, ID extends Serializable>
    extends BaseReadResource<T, ID> {

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected BaseReadWriteResource(IdentifiableObjectService<T, ID> identifiableObjectService,
                                    IdentifiableObjectRepository<T, ID> repository) {
        super(identifiableObjectService, repository);
    }

    @PostMapping("/bulk")
    public ResponseEntity<EntitySaveSummaryVM> saveAll(@RequestBody List<@Valid T> entities) {
        log.debug("REST request to saveAll all {}", getName());
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        for (T entity : entities) {
            saveEntity(entity, summary);
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping
    public ResponseEntity<EntitySaveSummaryVM> saveOne(@Valid @RequestBody T payLoadEntity) {
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        saveEntity(payLoadEntity, summary);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/return")
    public ResponseEntity<?> saveReturnSaved(@Valid @RequestBody T payLoadEntity) {
        log.info("Request to save and return {}:{}", payLoadEntity.getClass().getSimpleName(), payLoadEntity.getUid());
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        saveEntity(payLoadEntity, summary);
        if (payLoadEntity.getUid() != null) {
            return ResponseEntity.ok(payLoadEntity);
        }
        return ResponseEntity.ok(summary);
    }

//    protected T preProcess(List<T> payLoadEntity) {
        protected List<T> preProcess(List<T> payLoadEntity) {
        // do nothing
        return payLoadEntity;
    }

    protected void saveEntity(T payLoadEntity, EntitySaveSummaryVM summary) {
        final var user = SecurityUtils.getCurrentUserDetailsOrThrow();
        hasMinimalRightsOrThrow(user);
        var processedEntity = preProcess(List.of(payLoadEntity)).stream().findFirst().get();
        if (identifiableObjectService.findByIdOrUid(processedEntity).isPresent()) {
            if (aclService.canUpdate(payLoadEntity, user)) {
                processedEntity = identifiableObjectService.update(processedEntity);
                summary.getUpdated().add(processedEntity.getUid());
            } else {
                throw new CreateAccessDeniedException("You have no right to send things here");
            }
        } else {
            if (aclService.canAddNew(payLoadEntity, user)) {
                processedEntity = identifiableObjectService.save(processedEntity);
                summary.getCreated().add(processedEntity.getUid());
            } else {
                throw new UpdateAccessDeniedException("You have no right to send things here");
            }
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteByIdUid(@PathVariable("id") String id,
                                              @AuthenticationPrincipal CurrentUserDetails user) {
        hasMinimalRightsOrThrow(user);
        log.debug("REST request to delete from {}: {}", getName(), id);
        final var entity = identifiableObjectService.findByUid(id).orElseThrow();
        if (aclService.canDelete(entity, user)) {
            identifiableObjectService.delete(entity);
        } else {
            throw new DeleteAccessDeniedException("");
        }

        return ResponseEntity
            .noContent()
            .headers(HeaderUtil
                .createEntityDeletionAlert(applicationName, true, getName(), id)).build();
    }

    @PutMapping("/{uid}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public ResponseEntity<T> updateEntity(
        @PathVariable(value = "uid", required = false) final String uid,
        @Valid @RequestBody T entity, @AuthenticationPrincipal CurrentUserDetails user
    ) throws URISyntaxException {
        hasMinimalRightsOrThrow(user);
        log.debug("REST request to delete from {}: {}", getName(), uid);
        if (entity.getUid() == null) {
            throw new BadRequestAlertException("Invalid uid", getName(), "uid is null");
        }

        if (!Objects.equals(uid, entity.getUid())) {
            throw new BadRequestAlertException("Invalid ID", getName(), "idinvalid");
        }

        if (aclService.canUpdate(entity, user)) {
            entity = identifiableObjectService.update(entity);
        } else {
            throw new UpdateAccessDeniedException("AccessDenied");
        }


        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, getName(), entity.getId().toString()))
            .body(entity);
    }
}


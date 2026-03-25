package org.nmcpye.datarun.web.rest.v1.formtemplate;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.acl.AclService;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.service.DataTemplateInstanceService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.template.datatemplateprocessor.FormTemplateProcessor;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.nmcpye.datarun.web.rest.util.HeaderUtil;
import org.nmcpye.datarun.web.rest.util.ResponseUtil;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.nmcpye.datarun.web.rest.v1.formtemplate.FormTemplateMergeResource.V1;

@RestController
@RequestMapping(value = {V1})
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
@Slf4j
public class FormTemplateMergeResource {
    protected static final String NAME = "/dataFormTemplates";
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final FormTemplateProcessor formTemplateProcessor;
    private final DataTemplateInstanceService templateService;
    protected final AclService aclService;

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    public FormTemplateMergeResource(FormTemplateProcessor formTemplateProcessor, DataTemplateInstanceService templateService, AclService aclService) {
        this.formTemplateProcessor = formTemplateProcessor;
        this.templateService = templateService;
        this.aclService = aclService;
    }


    protected String getName() {
        return "dataFormTemplates";
    }

    /**
     * {@code GET  /Ts} : get all the entities.
     *
     * @param queryRequest the query request parameters.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of assignments in body.
     */
    @GetMapping(value = "")
    protected ResponseEntity<PagedResponse<?>> getAll(QueryRequest queryRequest,
                                                      @RequestBody(required = false) String jsonQueryBody) throws Exception {
        final var userLogin = SecurityUtils.getCurrentUserLoginOrThrow();
        log.debug("REST request to getAll {}:{}", userLogin, getName());

        Page<DataTemplateInstanceDto> processedPage = getList(queryRequest, jsonQueryBody);

        String next = PagingConfigurator.createNextPageLink(processedPage);

        PagedResponse<DataTemplateInstanceDto> response = PagingConfigurator.initPageResponse(processedPage, next, getName());
        return ResponseEntity.ok(response);
    }

    protected Page<DataTemplateInstanceDto> getList(QueryRequest queryRequest, String jsonQueryBody) {
        final var user = SecurityUtils.getCurrentUserDetailsOrThrow();
        hasMinimalRightsOrThrow(user);
        return templateService.findAllByUser(queryRequest, jsonQueryBody);
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @PostMapping("/bulk")
    public ResponseEntity<EntitySaveSummaryVM> saveAll(@Valid @RequestBody List<DataTemplateInstanceDto> entities) {
        log.debug("REST request to saveAll all {}", getName());
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        for (DataTemplateInstanceDto entity : entities) {
            saveEntity(entity, summary);
        }
        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @PostMapping
    public ResponseEntity<EntitySaveSummaryVM> saveOne(@Valid @RequestBody DataTemplateInstanceDto formTemplate) {
        log.debug("REST request to saveOne {}", getName());
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
//        final var validated = formTemplateProcessor.validate(formTemplate);

        this.saveEntity(formTemplate, summary);

        return ResponseEntity.ok(summary);
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @PostMapping("/return")
    public ResponseEntity<?> saveReturnSaved(@Valid @RequestBody DataTemplateInstanceDto entity) {
        log.info("Request to save and return {}:{}", entity.getClass().getSimpleName(), entity.getUid());
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        final var saved = saveEntity(entity, summary);

        return ResponseEntity.ok(Objects.requireNonNullElse(saved, summary));
    }

    protected DataTemplateInstanceDto saveEntity(DataTemplateInstanceDto payLoadEntity, EntitySaveSummaryVM summary) {
        var processedEntity = preProcess(payLoadEntity);
        try {
            if (payLoadEntity.getUid() != null && templateService.existsByUid(payLoadEntity.getUid())) {
                processedEntity = templateService.update(processedEntity);
                summary.getUpdated().add(processedEntity.getUid());
            } else {
                processedEntity = templateService.save(payLoadEntity);
                summary.getCreated().add(processedEntity.getUid());
            }
        } catch (Exception e) {
            log.error("REST Error Saving payLoadEntity {}:{}", "FormTemplate", payLoadEntity.getUid());
            summary.getFailed().put(payLoadEntity.getUid(), e.getMessage());
            throw new IllegalQueryException(e.getMessage());
        }
        return processedEntity;
    }

    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteByIdUid(@PathVariable("id") String id) {
        log.debug("REST request to delete from {}: {}", getName(), id);
        templateService.deleteByUid(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil
                .createEntityDeletionAlert(applicationName, true, getName(), id)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataTemplateInstanceDto> getById(@PathVariable("id") String id) {
        final var user = SecurityUtils.getCurrentUserDetailsOrThrow();
        hasMinimalRightsOrThrow(user);
        log.debug("REST request to get from {}: {}", getName(), id);
        Optional<DataTemplateInstanceDto> entity = templateService.findByUid(id);
        return ResponseUtil.wrapOrNotFound(entity);
    }

    /**
     * minimal Access rights or throw
     *
     * @param currentUser user
     * @throws ResponseStatusException exception if has no business here whatsoever (no minimal rights)
     */
    protected void hasMinimalRightsOrThrow(CurrentUserDetails currentUser) throws ResponseStatusException {
        if (currentUser == null || !aclService.hasMinimalRights(currentUser)) {
            log.warn("REST Prevent Access, no minimal rights `{}`:`{}`", getName(), currentUser);
            throw new AccessDeniedException("You Hava No Business Here");
        }
    }

    protected DataTemplateInstanceDto preProcess(DataTemplateInstanceDto entity) {
        return (DataTemplateInstanceDto) formTemplateProcessor
            .processMetadata(formTemplateProcessor.validate(entity));
    }
}


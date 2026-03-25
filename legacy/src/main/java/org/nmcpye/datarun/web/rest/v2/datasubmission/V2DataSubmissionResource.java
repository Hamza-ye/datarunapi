package org.nmcpye.datarun.web.rest.v2.datasubmission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.service.DataSubmissionService;
import org.nmcpye.datarun.datacapture.datasubmission.validation.CompositeSubmissionValidator;
import org.nmcpye.datarun.datacapture.datasubmission.validation.SubmissionAccessValidator;
import org.nmcpye.datarun.datacapture.datasubmission.service.MigrationRepeatIdGenerator;
import org.nmcpye.datarun.template.datatemplate.service.TemplateElementService;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.web.rest.v2.datasubmission.service.SubmissionTranslationService;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.rest.v2.dto.V2SubmissionCreateRequest;
import org.nmcpye.datarun.web.rest.v2.dto.V2SubmissionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * V2 REST controller for data submissions.
 * <p>
 * Accepts and returns the canonical V2 shape (§4).
 * Internally translates to/from V1 shape for storage
 * (storage model unchanged — Phase 2 scope).
 *
 * @author Hamza Assada
 */
@RestController
@RequestMapping(ApiVersion.API_V2 + "/dataSubmission")
@PreAuthorize("hasAnyAuthority('" + AuthoritiesConstants.ADMIN + "', '" + AuthoritiesConstants.USER + "')")
public class V2DataSubmissionResource {

    private static final Logger log = LoggerFactory.getLogger(V2DataSubmissionResource.class);

    private final DataSubmissionService submissionService;
    private final SubmissionTranslationService translationService;
    private final ObjectMapper objectMapper;
    private final CompositeSubmissionValidator compositeValidator;
    private final SubmissionAccessValidator submissionAccessValidator;
    private final TemplateElementService templateElementService;

    public V2DataSubmissionResource(DataSubmissionService submissionService,
            SubmissionTranslationService translationService,
            ObjectMapper objectMapper,
            CompositeSubmissionValidator compositeValidator,
            SubmissionAccessValidator submissionAccessValidator,
            TemplateElementService templateElementService) {
        this.submissionService = submissionService;
        this.translationService = translationService;
        this.objectMapper = objectMapper;
        this.compositeValidator = compositeValidator;
        this.submissionAccessValidator = submissionAccessValidator;
        this.templateElementService = templateElementService;
    }

    /**
     * {@code GET /api/v2/dataSubmission/:uid} — Get a submission in V2 canonical
     * shape.
     *
     * @param uid the submission UID
     * @return V2SubmissionDto
     */
    @GetMapping("/{uid}")
    public ResponseEntity<V2SubmissionDto> getByUid(@PathVariable("uid") String uid) {
        log.debug("V2 REST request to get DataSubmission: {}", uid);

        DataSubmission submission = submissionService.findByUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("DataSubmission not found: " + uid));

        V2SubmissionDto dto = toV2Dto(submission);
        return ResponseEntity.ok(dto);
    }

    /**
     * {@code POST /api/v2/dataSubmission} — Create/update a submission from V2
     * canonical shape.
     * <p>
     * Flow: V2 canonical → denormalize to V1 → validate → upsert → return V2
     * response.
     *
     * @param request the V2 submission create request
     * @return V2SubmissionDto of the saved entity
     */
    @PostMapping
    public ResponseEntity<V2SubmissionDto> createOrUpdate(@Valid @RequestBody V2SubmissionCreateRequest request) {
        log.debug("V2 REST request to save DataSubmission: templateUid={}", request.getTemplateUid());

        // 1. Reassemble canonical formData from values + collections
        JsonNode canonicalFormData = assembleCanonicalFormData(request.getValues(), request.getCollections());

        // 2. Denormalize canonical → V1 shape for storage
        JsonNode v1FormData = translationService.denormalizeCanonicalToV1(
                canonicalFormData,
                request.getTemplateUid(),
                request.getVersionUid(),
                request.getSubmissionUid());

        // 3. Build the DataSubmission entity
        DataSubmission entity = buildEntity(request, v1FormData);

        // 4. Pre-process: generate repeat IDs (same as V1 flow)
        ObjectNode root = (ObjectNode) (entity.getFormData() == null
                ? objectMapper.createObjectNode()
                : entity.getFormData().deepCopy());
        MigrationRepeatIdGenerator idGen = new MigrationRepeatIdGenerator(
                templateElementService.getTemplateElementMap(entity.getForm(), entity.getFormVersion()));
        int generated = idGen.generateMissingIdsForMigration(root, entity.getUid());
        if (generated > 0) {
            entity.setFormData(root);
        }

        // 5. Validate access + business rules (same as V1 flow)
        compositeValidator.validateAndEnrich(
                submissionAccessValidator.validateAccess(entity, SecurityUtils.getCurrentUserDetailsOrThrow()));

        // 6. Upsert
        EntitySaveSummaryVM summary = new EntitySaveSummaryVM();
        DataSubmission saved = submissionService.upsert(entity, SecurityUtils.getCurrentUserDetailsOrThrow(), summary);

        // 7. Return V2 response (normalize the saved entity back to canonical)
        V2SubmissionDto dto = toV2Dto(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Convert a DataSubmission entity to V2SubmissionDto.
     * Normalizes the V1 formData to canonical shape.
     */
    private V2SubmissionDto toV2Dto(DataSubmission submission) {
        JsonNode canonicalFormData = translationService.normalizeV1ToCanonical(
                submission.getFormData(),
                submission.getForm(),
                submission.getFormVersion());

        JsonNode values = canonicalFormData.has("values") ? canonicalFormData.get("values") : null;
        JsonNode collections = canonicalFormData.has("collections") ? canonicalFormData.get("collections") : null;

        return V2SubmissionDto.builder()
                .submissionUid(submission.getUid())
                .templateUid(submission.getForm())
                .versionUid(submission.getFormVersion())
                .versionNumber(submission.getVersion())
                .status(submission.getStatus() != null ? submission.getStatus().name() : null)
                .team(submission.getTeam())
                .orgUnit(submission.getOrgUnit())
                .activity(submission.getActivity())
                .assignment(submission.getAssignment())
                .startEntryTime(submission.getStartEntryTime())
                .finishedEntryTime(submission.getFinishedEntryTime())
                .values(values)
                .collections(collections)
                .build();
    }

    /**
     * Reassemble values + collections into a single canonical formData node.
     */
    private JsonNode assembleCanonicalFormData(JsonNode values, JsonNode collections) {
        ObjectNode canonical = objectMapper.createObjectNode();
        if (values != null) {
            canonical.set("values", values);
        }
        if (collections != null) {
            canonical.set("collections", collections);
        }
        return canonical;
    }

    /**
     * Build DataSubmission entity from V2 request + denormalized V1 formData.
     */
    private DataSubmission buildEntity(V2SubmissionCreateRequest request, JsonNode v1FormData) {
        DataSubmission entity = new DataSubmission();
        entity.setUid(request.getSubmissionUid());
        entity.setForm(request.getTemplateUid());
        entity.setFormVersion(request.getVersionUid());
        entity.setVersion(request.getVersionNumber());
        entity.setFormData(v1FormData);

        if (request.getStatus() != null) {
            try {
                entity.setStatus(FlowStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown status '{}', defaulting to null", request.getStatus());
            }
        }

        entity.setTeam(request.getTeam());
        entity.setOrgUnit(request.getOrgUnit());
        entity.setActivity(request.getActivity());
        entity.setAssignment(request.getAssignment());
        entity.setStartEntryTime(request.getStartEntryTime());
        entity.setFinishedEntryTime(request.getFinishedEntryTime());

        return entity;
    }
}

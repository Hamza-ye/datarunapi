package org.nmcpye.datarun.web.rest.v1.datasubmission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.util.ResponseUtil;
import org.nmcpye.datarun.web.rest.v1.datasubmission.dto.DataSubmissionV1Dto;
import org.nmcpye.datarun.web.rest.v1.datasubmission.service.DataSubmissionV1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link DataSubmission}.
 * Refactored to use DTOs for API Surface Stabilization (Strangler Fig Phase 0).
 */
@RestController
@RequestMapping(value = { DataSubmissionResource.CUSTOM, DataSubmissionResource.V1 })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
@RequiredArgsConstructor
@Slf4j
public class DataSubmissionResource {
    protected static final String NAME = "/dataSubmission";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final DataSubmissionV1Service v1Service;

    @GetMapping("")
    public ResponseEntity<PagedResponse<DataSubmissionV1Dto>> getAll(QueryRequest queryRequest) {
        log.debug("REST request to get all DataSubmissions");
        return ResponseEntity.ok(v1Service.getAll(queryRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataSubmissionV1Dto> getById(@PathVariable("id") String id) {
        log.debug("REST request to get DataSubmission : {}", id);
        Optional<DataSubmissionV1Dto> dto = v1Service.getById(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }

    @PostMapping("/bulk")
    public ResponseEntity<EntitySaveSummaryVM> saveAll(@RequestBody List<DataSubmissionV1Dto> dtoList) {
        log.debug("REST request to save {} DataSubmissions", dtoList.size());
        EntitySaveSummaryVM summary = v1Service.upsertAll(dtoList);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("")
    public ResponseEntity<EntitySaveSummaryVM> saveOne(@RequestBody DataSubmissionV1Dto submission) {
        log.debug("REST request to save {} DataSubmission", submission.getUid());
        EntitySaveSummaryVM summary = v1Service.upsertAll(List.of(submission));
        return ResponseEntity.ok(summary);
    }
}

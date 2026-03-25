package org.nmcpye.datarun.web.rest.v1.team;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.iam.security.AuthoritiesConstants;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.ApiVersion;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.util.ResponseUtil;
import org.nmcpye.datarun.web.rest.v1.team.dto.TeamV1Dto;
import org.nmcpye.datarun.web.rest.v1.team.service.TeamV1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * V1 REST controller for managing Teams.
 * Standalone / no JpaBaseResource inheritance.
 * Read-only for mobile app consumption.
 */
@RestController
@RequestMapping(value = { TeamResource.CUSTOM, TeamResource.V1 })
@PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\", \"" + AuthoritiesConstants.USER + "\")")
@RequiredArgsConstructor
@Slf4j
public class TeamResource {
    protected static final String NAME = "/teams";
    protected static final String CUSTOM = ApiVersion.API_CUSTOM + NAME;
    protected static final String V1 = ApiVersion.API_V1 + NAME;

    private final TeamV1Service v1Service;

    @GetMapping("")
    public ResponseEntity<PagedResponse<TeamV1Dto>> getAll(QueryRequest queryRequest) {
        log.debug("REST request to get all Teams");
        return ResponseEntity.ok(v1Service.getAll(queryRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamV1Dto> getById(@PathVariable("id") String id) {
        log.debug("REST request to get Team : {}", id);
        Optional<TeamV1Dto> dto = v1Service.getById(id);
        return ResponseUtil.wrapOrNotFound(dto);
    }
}

package org.nmcpye.datarun.web.rest.v1.team.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.team.dto.TeamV1Dto;

import java.util.Optional;

/**
 * V1 service interface for Team read operations.
 */
public interface TeamV1Service {

    PagedResponse<TeamV1Dto> getAll(QueryRequest queryRequest);

    Optional<TeamV1Dto> getById(String id);
}

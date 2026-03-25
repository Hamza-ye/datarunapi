package org.nmcpye.datarun.web.rest.v1.orgunit.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.orgunit.dto.OrgUnitV1Dto;

import java.util.Optional;

/**
 * V1 service interface for OrgUnit read operations.
 */
public interface OrgUnitV1Service {

    PagedResponse<OrgUnitV1Dto> getAll(QueryRequest queryRequest);

    Optional<OrgUnitV1Dto> getById(String id);
}

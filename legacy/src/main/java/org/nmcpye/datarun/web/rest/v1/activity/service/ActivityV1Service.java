package org.nmcpye.datarun.web.rest.v1.activity.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.activity.dto.ActivityV1Dto;

import java.util.Optional;

/**
 * V1 service interface for Activity read operations.
 */
public interface ActivityV1Service {

    PagedResponse<ActivityV1Dto> getAll(QueryRequest queryRequest);

    Optional<ActivityV1Dto> getById(String id);
}

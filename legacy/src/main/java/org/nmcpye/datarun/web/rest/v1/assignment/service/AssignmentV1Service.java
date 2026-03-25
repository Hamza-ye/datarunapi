package org.nmcpye.datarun.web.rest.v1.assignment.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentV1Dto;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentWithAccessV1Dto;

import java.util.Optional;

public interface AssignmentV1Service {
    PagedResponse<AssignmentV1Dto> getAll(QueryRequest queryRequest);

    PagedResponse<AssignmentWithAccessV1Dto> getAllWithAccess(QueryRequest queryRequest, String jsonQuery);

    Optional<AssignmentV1Dto> getById(String id);
}

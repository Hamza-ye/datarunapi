package org.nmcpye.datarun.web.rest.v1.project.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.project.dto.ProjectV1Dto;

import java.util.Optional;

/**
 * V1 service interface for Project read operations.
 */
public interface ProjectV1Service {

    PagedResponse<ProjectV1Dto> getAll(QueryRequest queryRequest);

    Optional<ProjectV1Dto> getById(String id);
}

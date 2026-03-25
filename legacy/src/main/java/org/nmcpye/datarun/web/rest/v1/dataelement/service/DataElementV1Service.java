package org.nmcpye.datarun.web.rest.v1.dataelement.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.dataelement.dto.DataElementV1Dto;

import java.util.Optional;

public interface DataElementV1Service {

    PagedResponse<DataElementV1Dto> getAll(QueryRequest queryRequest);

    Optional<DataElementV1Dto> getById(String id);
}

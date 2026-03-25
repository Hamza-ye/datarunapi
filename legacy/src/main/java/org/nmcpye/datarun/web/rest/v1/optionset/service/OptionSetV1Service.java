package org.nmcpye.datarun.web.rest.v1.optionset.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.optionset.dto.OptionSetV1Dto;

import java.util.Optional;

public interface OptionSetV1Service {

    PagedResponse<OptionSetV1Dto> getAll(QueryRequest queryRequest);

    Optional<OptionSetV1Dto> getById(String id);
}

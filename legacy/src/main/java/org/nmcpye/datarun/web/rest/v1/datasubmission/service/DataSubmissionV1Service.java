package org.nmcpye.datarun.web.rest.v1.datasubmission.service;

import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.datasubmission.dto.DataSubmissionV1Dto;

import java.util.List;
import java.util.Optional;

public interface DataSubmissionV1Service {
    PagedResponse<DataSubmissionV1Dto> getAll(QueryRequest queryRequest);

    Optional<DataSubmissionV1Dto> getById(String id);

    EntitySaveSummaryVM upsertAll(List<DataSubmissionV1Dto> dtoList);
}

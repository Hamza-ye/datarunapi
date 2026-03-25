package org.nmcpye.datarun.web.rest.v1.formtemplate.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.FormTemplateV1Dto;

import java.util.Optional;

public interface FormTemplateV1Service {

    PagedResponse<FormTemplateV1Dto> getAll(QueryRequest queryRequest);

    Optional<FormTemplateV1Dto> getById(String id);
}

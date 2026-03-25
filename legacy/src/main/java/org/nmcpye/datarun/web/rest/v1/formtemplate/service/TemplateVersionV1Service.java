package org.nmcpye.datarun.web.rest.v1.formtemplate.service;

import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.TemplateVersionV1Dto;

import java.util.Optional;

public interface TemplateVersionV1Service {

    PagedResponse<TemplateVersionV1Dto> getAll(QueryRequest queryRequest);

    Optional<TemplateVersionV1Dto> getById(String id);
}

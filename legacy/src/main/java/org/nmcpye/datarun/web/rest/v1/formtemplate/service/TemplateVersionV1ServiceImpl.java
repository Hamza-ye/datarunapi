package org.nmcpye.datarun.web.rest.v1.formtemplate.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.service.TemplateVersionService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.TemplateVersionV1Dto;
import org.nmcpye.datarun.web.rest.v1.formtemplate.mapper.TemplateVersionV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemplateVersionV1ServiceImpl implements TemplateVersionV1Service {

    private final TemplateVersionService templateVersionService;
    private final TemplateVersionV1Mapper templateVersionMapper;

    @Override
    public PagedResponse<TemplateVersionV1Dto> getAll(QueryRequest queryRequest) {
        Page<TemplateVersion> page = templateVersionService.findAllByUser(queryRequest, null);
        Page<TemplateVersionV1Dto> dtoPage = page.map(templateVersionMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "formTemplateVersions");
    }

    @Override
    public Optional<TemplateVersionV1Dto> getById(String id) {
        return templateVersionService.findByIdOrUid(id).map(templateVersionMapper::toDto);
    }
}

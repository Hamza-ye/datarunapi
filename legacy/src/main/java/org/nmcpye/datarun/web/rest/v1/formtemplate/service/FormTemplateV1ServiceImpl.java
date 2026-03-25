package org.nmcpye.datarun.web.rest.v1.formtemplate.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.service.DataTemplateService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.FormTemplateV1Dto;
import org.nmcpye.datarun.web.rest.v1.formtemplate.mapper.FormTemplateV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormTemplateV1ServiceImpl implements FormTemplateV1Service {

    private final DataTemplateService dataTemplateService;
    private final FormTemplateV1Mapper formTemplateMapper;

    @Override
    public PagedResponse<FormTemplateV1Dto> getAll(QueryRequest queryRequest) {
        Page<DataTemplate> page = dataTemplateService.findAllByUser(queryRequest, null);
        Page<FormTemplateV1Dto> dtoPage = page.map(formTemplateMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "formTemplates");
    }

    @Override
    public Optional<FormTemplateV1Dto> getById(String id) {
        return dataTemplateService.findByIdOrUid(id).map(formTemplateMapper::toDto);
    }
}

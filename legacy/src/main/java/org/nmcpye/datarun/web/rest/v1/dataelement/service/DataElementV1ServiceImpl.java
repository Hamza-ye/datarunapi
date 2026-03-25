package org.nmcpye.datarun.web.rest.v1.dataelement.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.template.dataelement.service.DataElementService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.dataelement.dto.DataElementV1Dto;
import org.nmcpye.datarun.web.rest.v1.dataelement.mapper.DataElementV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DataElementV1ServiceImpl implements DataElementV1Service {

    private final DataElementService dataElementService;
    private final DataElementV1Mapper dataElementMapper;

    @Override
    public PagedResponse<DataElementV1Dto> getAll(QueryRequest queryRequest) {
        Page<DataElement> page = dataElementService.findAllByUser(queryRequest, null);
        Page<DataElementV1Dto> dtoPage = page.map(dataElementMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "dataElements");
    }

    @Override
    public Optional<DataElementV1Dto> getById(String id) {
        return dataElementService.findByIdOrUid(id).map(dataElementMapper::toDto);
    }
}

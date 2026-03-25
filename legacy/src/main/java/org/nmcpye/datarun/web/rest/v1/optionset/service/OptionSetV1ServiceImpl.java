package org.nmcpye.datarun.web.rest.v1.optionset.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.jpa.option.OptionSet;
import org.nmcpye.datarun.jpa.option.service.OptionSetService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.optionset.dto.OptionSetV1Dto;
import org.nmcpye.datarun.web.rest.v1.optionset.mapper.OptionSetV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OptionSetV1ServiceImpl implements OptionSetV1Service {

    private final OptionSetService optionSetService;
    private final OptionSetV1Mapper optionSetMapper;

    @Override
    public PagedResponse<OptionSetV1Dto> getAll(QueryRequest queryRequest) {
        Page<OptionSet> page = optionSetService.findAllByUser(queryRequest, null);
        Page<OptionSetV1Dto> dtoPage = page.map(optionSetMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "optionSets");
    }

    @Override
    public Optional<OptionSetV1Dto> getById(String id) {
        return optionSetService.findByIdOrUid(id).map(optionSetMapper::toDto);
    }
}

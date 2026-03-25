package org.nmcpye.datarun.web.rest.v1.orgunit.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.jpa.orgunit.service.OrgUnitService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.orgunit.dto.OrgUnitV1Dto;
import org.nmcpye.datarun.web.rest.v1.orgunit.mapper.OrgUnitV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrgUnitV1ServiceImpl implements OrgUnitV1Service {

    private final OrgUnitService orgUnitService;
    private final OrgUnitV1Mapper orgUnitMapper;

    @Override
    public PagedResponse<OrgUnitV1Dto> getAll(QueryRequest queryRequest) {
        Page<OrgUnit> page = orgUnitService.findAllByUser(queryRequest, null);
        Page<OrgUnitV1Dto> dtoPage = page.map(orgUnitMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "orgUnits");
    }

    @Override
    public Optional<OrgUnitV1Dto> getById(String id) {
        return orgUnitService.findByIdOrUid(id).map(orgUnitMapper::toDto);
    }
}

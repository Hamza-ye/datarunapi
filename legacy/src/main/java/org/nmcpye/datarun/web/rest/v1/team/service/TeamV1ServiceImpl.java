package org.nmcpye.datarun.web.rest.v1.team.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.team.service.TeamService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.nmcpye.datarun.web.rest.v1.team.dto.TeamV1Dto;
import org.nmcpye.datarun.web.rest.v1.team.mapper.TeamV1Mapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamV1ServiceImpl implements TeamV1Service {

    private final TeamService teamService;
    private final TeamV1Mapper teamMapper;

    @Override
    public PagedResponse<TeamV1Dto> getAll(QueryRequest queryRequest) {
        Page<Team> page = teamService.findAllByUser(queryRequest, null);
        Page<TeamV1Dto> dtoPage = page.map(teamMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "teams");
    }

    @Override
    public Optional<TeamV1Dto> getById(String id) {
        return teamService.findByIdOrUid(id).map(teamMapper::toDto);
    }
}

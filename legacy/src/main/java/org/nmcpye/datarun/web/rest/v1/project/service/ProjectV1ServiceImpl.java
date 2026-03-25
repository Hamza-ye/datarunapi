package org.nmcpye.datarun.web.rest.v1.project.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.assignment.project.service.ProjectService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.nmcpye.datarun.web.rest.v1.project.dto.ProjectV1Dto;
import org.nmcpye.datarun.web.rest.v1.project.mapper.ProjectV1Mapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectV1ServiceImpl implements ProjectV1Service {

    private final ProjectService projectService;
    private final ProjectV1Mapper projectMapper;

    @Override
    public PagedResponse<ProjectV1Dto> getAll(QueryRequest queryRequest) {
        Page<Project> page = projectService.findAllByUser(queryRequest, null);
        Page<ProjectV1Dto> dtoPage = page.map(projectMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "projects");
    }

    @Override
    public Optional<ProjectV1Dto> getById(String id) {
        return projectService.findByIdOrUid(id).map(projectMapper::toDto);
    }
}

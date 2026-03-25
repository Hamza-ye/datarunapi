package org.nmcpye.datarun.web.rest.v1.assignment.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.service.AssignmentService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentV1Dto;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentWithAccessV1Dto;
import org.nmcpye.datarun.web.rest.v1.assignment.mapper.AssignmentV1Mapper;
import org.nmcpye.datarun.web.rest.v1.assignment.mapper.AssignmentWithAccessV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AssignmentV1ServiceImpl implements AssignmentV1Service {

    private final AssignmentService assignmentService;
    private final AssignmentV1Mapper assignmentMapper;
    private final AssignmentWithAccessV1Mapper assignmentWithAccessMapper;

    @Override
    public PagedResponse<AssignmentV1Dto> getAll(QueryRequest queryRequest) {
        Page<Assignment> page = assignmentService.findAllByUser(queryRequest, null);
        Page<AssignmentV1Dto> dtoPage = page.map(assignmentMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "assignments");
    }

    @Override
    public PagedResponse<AssignmentWithAccessV1Dto> getAllWithAccess(QueryRequest queryRequest, String jsonQuery) {
        // We bypass the old DTO-returning service method to use our V1 mapper
        Page<Assignment> page = assignmentService.findAllByUser(queryRequest, jsonQuery);
        Page<AssignmentWithAccessV1Dto> dtoPage = page.map(assignmentWithAccessMapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "assignments");
    }

    @Override
    public Optional<AssignmentV1Dto> getById(String id) {
        return assignmentService.findByIdOrUid(id).map(assignmentMapper::toDto);
    }
}

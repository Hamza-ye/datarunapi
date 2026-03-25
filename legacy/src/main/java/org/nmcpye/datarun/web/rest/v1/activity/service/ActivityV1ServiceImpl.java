package org.nmcpye.datarun.web.rest.v1.activity.service;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.activity.service.ActivityService;
import org.nmcpye.datarun.sharedkernal.apiquery.QueryRequest;
import org.nmcpye.datarun.web.common.PagedResponse;
import org.nmcpye.datarun.web.rest.v1.activity.dto.ActivityV1Dto;
import org.nmcpye.datarun.web.rest.v1.activity.mapper.ActivityV1Mapper;
import org.nmcpye.datarun.web.rest.v1.paging.PagingConfigurator;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ActivityV1ServiceImpl implements ActivityV1Service {

    private final ActivityService activityService;
    private final ActivityV1Mapper activityV1Mapper;

    @Override
    public PagedResponse<ActivityV1Dto> getAll(QueryRequest queryRequest) {
        Page<Activity> page = activityService.findAllByUser(queryRequest, null);
        Page<ActivityV1Dto> dtoPage = page.map(activityV1Mapper::toDto);
        String next = PagingConfigurator.createNextPageLink(dtoPage);
        return PagingConfigurator.initPageResponse(dtoPage, next, "activities");
    }

    @Override
    public Optional<ActivityV1Dto> getById(String id) {
        return activityService.findByIdOrUid(id).map(activityV1Mapper::toDto);
    }
}

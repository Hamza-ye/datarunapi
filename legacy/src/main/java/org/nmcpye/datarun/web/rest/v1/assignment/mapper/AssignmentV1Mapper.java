package org.nmcpye.datarun.web.rest.v1.assignment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentV1Dto;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AssignmentV1Mapper {

    @Mapping(target = "activityUid", source = "activity.uid")
    @Mapping(target = "teamUid", source = "team.uid")
    @Mapping(target = "orgUnitUid", source = "orgUnit.uid")
    @Mapping(target = "parentUid", source = "parent.uid")
    AssignmentV1Dto toDto(Assignment entity);

    @Mapping(target = "activity.uid", source = "activityUid")
    @Mapping(target = "team.uid", source = "teamUid")
    @Mapping(target = "orgUnit.uid", source = "orgUnitUid")
    @Mapping(target = "parent.uid", source = "parentUid")
    Assignment toEntity(AssignmentV1Dto dto);

    List<AssignmentV1Dto> toDtoList(List<Assignment> entities);
}

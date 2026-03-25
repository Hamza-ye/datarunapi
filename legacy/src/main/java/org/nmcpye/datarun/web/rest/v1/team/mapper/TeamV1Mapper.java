package org.nmcpye.datarun.web.rest.v1.team.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.web.rest.v1.team.dto.TeamV1Dto;

import java.util.List;

/**
 * One-way mapper: Team entity → TeamV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TeamV1Mapper {

    @Mapping(target = "activityUid", source = "activity.uid")
    TeamV1Dto toDto(Team entity);

    List<TeamV1Dto> toDtoList(List<Team> entities);
}

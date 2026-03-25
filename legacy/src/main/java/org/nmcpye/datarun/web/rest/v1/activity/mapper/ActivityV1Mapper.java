package org.nmcpye.datarun.web.rest.v1.activity.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.web.rest.v1.activity.dto.ActivityV1Dto;
import org.nmcpye.datarun.web.rest.v1.common.LabelTranslationGetter;

import java.util.List;

/**
 * One-way mapper: Activity entity → ActivityV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ActivityV1Mapper extends LabelTranslationGetter {
    @Mapping(target = "project.id", source = "project.id")
    @Mapping(target = "project.uid", source = "project.uid")
    @Mapping(target = "project.code", source = "project.code")
    @Mapping(target = "project.name", source = "project.name")
    @Mapping(target = "label", source = "translations", qualifiedByName = "labelTranslationGetter")
    ActivityV1Dto toDto(Activity entity);

    List<ActivityV1Dto> toDtoList(List<Activity> entities);

}

package org.nmcpye.datarun.web.rest.v1.project.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.assignment.project.Project;
import org.nmcpye.datarun.web.rest.v1.common.LabelTranslationGetter;
import org.nmcpye.datarun.web.rest.v1.project.dto.ProjectV1Dto;

import java.util.List;

/**
 * One-way mapper: Project entity → ProjectV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectV1Mapper extends LabelTranslationGetter {
    @Mapping(target = "label", source = "translations", qualifiedByName = "labelTranslationGetter")
    ProjectV1Dto toDto(Project entity);

    List<ProjectV1Dto> toDtoList(List<Project> entities);
}

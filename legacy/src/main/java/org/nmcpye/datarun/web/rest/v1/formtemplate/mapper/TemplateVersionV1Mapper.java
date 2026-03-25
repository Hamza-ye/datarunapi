package org.nmcpye.datarun.web.rest.v1.formtemplate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.TemplateVersionV1Dto;

import java.util.List;

/**
 * One-way mapper: TemplateVersion entity → TemplateVersionV1Dto (read-only
 * facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TemplateVersionV1Mapper {

    TemplateVersionV1Dto toDto(TemplateVersion entity);

    List<TemplateVersionV1Dto> toDtoList(List<TemplateVersion> entities);
}

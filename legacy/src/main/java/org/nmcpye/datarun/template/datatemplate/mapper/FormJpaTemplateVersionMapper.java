package org.nmcpye.datarun.template.datatemplate.mapper;

import org.mapstruct.*;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.dto.FormTemplateVersionDto;
import org.nmcpye.datarun.sharedkernal.BaseMapper;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FormJpaTemplateVersionMapper
        extends BaseMapper<FormTemplateVersionDto, TemplateVersion> {

    @Mappings({
            @Mapping(target = "uid", ignore = true),
            // @Mapping(target = "id", ignore = true),
            @Mapping(target = "versionNumber", ignore = true),
            @Mapping(target = "templateUid", source = "uid"),
    })
    TemplateVersion fromInstanceDto(DataTemplateInstanceDto dto);
}

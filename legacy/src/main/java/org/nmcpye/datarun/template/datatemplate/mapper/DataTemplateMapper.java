package org.nmcpye.datarun.template.datatemplate.mapper;

import org.mapstruct.*;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateDto;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.dto.FormTemplateVersionDto;
import org.nmcpye.datarun.sharedkernal.BaseMapper;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface DataTemplateMapper
    extends BaseMapper<DataTemplateDto, DataTemplate> {
    @Mappings({
        @Mapping(target = "versionUid", ignore = true),
        @Mapping(target = "versionNumber", ignore = true),
        @Mapping(target = "uid", source = "uid"),
    })
    DataTemplate fromInstanceDto(DataTemplateInstanceDto dto);

    @Mappings({
        @Mapping(target = "id", source = "templateDto.id"),
        @Mapping(target = "uid", source = "templateDto.uid"),
        @Mapping(target = "code", source = "templateDto.code"),
        @Mapping(target = "deleted", source = "templateDto.deleted"),
        @Mapping(target = "name", source = "templateDto.name"),
        @Mapping(target = "description", source = "templateDto.description"),
        @Mapping(target = "label", source = "templateDto.label"),
        @Mapping(target = "versionNumber", source = "templateDto.versionNumber"),
        @Mapping(target = "versionUid", source = "templateDto.versionUid"),
        @Mapping(target = "fields", source = "versionDto.fields"),
        @Mapping(target = "sections", source = "versionDto.sections"),
    })
    DataTemplateInstanceDto toInstanceDto(DataTemplateDto templateDto, FormTemplateVersionDto versionDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DataTemplate partialUpdate(DataTemplateDto dataTemplateDto, @MappingTarget DataTemplate dataTemplate);
}

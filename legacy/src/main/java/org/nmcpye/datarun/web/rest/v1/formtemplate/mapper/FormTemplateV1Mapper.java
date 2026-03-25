package org.nmcpye.datarun.web.rest.v1.formtemplate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.web.rest.v1.common.LabelTranslationGetter;
import org.nmcpye.datarun.web.rest.v1.formtemplate.dto.FormTemplateV1Dto;

import java.util.List;

/**
 * One-way mapper: DataTemplate entity → FormTemplateV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FormTemplateV1Mapper extends LabelTranslationGetter {
    @Mapping(target = "label", source = "translations", qualifiedByName = "labelTranslationGetter")
    FormTemplateV1Dto toDto(DataTemplate entity);

    List<FormTemplateV1Dto> toDtoList(List<DataTemplate> entities);
}

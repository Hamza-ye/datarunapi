package org.nmcpye.datarun.web.rest.v1.dataelement.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.web.rest.v1.common.LabelTranslationGetter;
import org.nmcpye.datarun.web.rest.v1.dataelement.dto.DataElementV1Dto;

import java.util.List;

/**
 * One-way mapper: DataElement entity → DataElementV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DataElementV1Mapper extends LabelTranslationGetter {

    @Mapping(target = "optionSet.id", source = "optionSet.id")
    @Mapping(target = "optionSet.uid", source = "optionSet.uid")
    @Mapping(target = "optionSet.code", source = "optionSet.code")
    @Mapping(target = "optionSet.name", source = "optionSet.name")
    @Mapping(target = "label", source = "translations", qualifiedByName = "labelTranslationGetter")
    DataElementV1Dto toDto(DataElement entity);

    List<DataElementV1Dto> toDtoList(List<DataElement> entities);
}

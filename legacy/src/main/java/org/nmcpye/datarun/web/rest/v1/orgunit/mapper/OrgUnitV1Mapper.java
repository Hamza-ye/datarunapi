package org.nmcpye.datarun.web.rest.v1.orgunit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.web.rest.v1.common.LabelTranslationGetter;
import org.nmcpye.datarun.web.rest.v1.orgunit.dto.OrgUnitV1Dto;

import java.util.List;

/**
 * One-way mapper: OrgUnit entity → OrgUnitV1Dto (read-only facade).
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrgUnitV1Mapper extends LabelTranslationGetter {

    @Mapping(target = "parent.id", source = "parent.id")
    @Mapping(target = "parent.uid", source = "parent.uid")
    @Mapping(target = "parent.code", source = "parent.code")
    @Mapping(target = "parent.name", source = "parent.name")
    @Mapping(target = "label", source = "translations", qualifiedByName = "labelTranslationGetter")
    OrgUnitV1Dto toDto(OrgUnit entity);

    List<OrgUnitV1Dto> toDtoList(List<OrgUnit> entities);
}

package org.nmcpye.datarun.assignment.assignment.mapper;

import org.mapstruct.*;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.dto.AssignmentWithAccessDto;
import org.nmcpye.datarun.template.datatemplateprocessor.FormAccessService;
import org.nmcpye.datarun.sharedkernal.BaseMapper;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = FormAccessService.class)
public interface AssignmentWithAccessMapper
                extends BaseMapper<AssignmentWithAccessDto, Assignment> {

        @Mappings({
                        @Mapping(target = "id", source = "id"),
                        @Mapping(target = "activity.id", source = "activity"),
                        @Mapping(target = "orgUnit.id", source = "orgUnit"),
                        @Mapping(target = "team.id", source = "team"),
                        @Mapping(target = "status", source = "progressStatus"),
        })
        Assignment toEntity(AssignmentWithAccessDto dto);

        @Mappings({
                        @Mapping(target = "activity", source = "activity.id"),
                        @Mapping(source = "id", target = "id"),
                        @Mapping(target = "orgUnit", source = "orgUnit.id"),
                        @Mapping(target = "team", source = "team.id"),
                        @Mapping(target = "progressStatus", source = "status", defaultValue = "PLANNED"),
                        @Mapping(target = "accessibleForms", source = "assignment", qualifiedByName = "assignmentUserForms"),
        })
        AssignmentWithAccessDto toDto(Assignment assignment);

        @InheritConfiguration(name = "toEntity")
        @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
        Assignment partialUpdate(@MappingTarget Assignment stepType, AssignmentWithAccessDto stepTypeDto);
}

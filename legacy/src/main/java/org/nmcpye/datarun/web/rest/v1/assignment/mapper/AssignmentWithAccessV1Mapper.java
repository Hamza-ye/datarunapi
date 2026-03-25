package org.nmcpye.datarun.web.rest.v1.assignment.mapper;

import org.mapstruct.*;
import org.nmcpye.datarun.template.datatemplateprocessor.FormAccessService;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.dto.AssignmentFormDto;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentFormV1Dto;
import org.nmcpye.datarun.web.rest.v1.assignment.dto.AssignmentWithAccessV1Dto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    FormAccessService.class})
public abstract class AssignmentWithAccessV1Mapper {

    @Autowired
    protected FormAccessService formAccessService;

    @Mappings({
        @Mapping(target = "activity", source = "activity.uid"),
        @Mapping(target = "orgUnit", source = "orgUnit.uid"),
        @Mapping(target = "team", source = "team.uid"),
        @Mapping(target = "progressStatus", source = "status", defaultValue = "PLANNED"),
        @Mapping(target = "accessibleForms", source = "assignment", qualifiedByName = "assignmentUserFormsV1"),
    })
    public abstract AssignmentWithAccessV1Dto toDto(Assignment assignment);

    public AssignmentFormV1Dto toV1Dto(AssignmentFormDto dto) {
        if (dto == null) {
            return null;
        }
        return AssignmentFormV1Dto.builder()
            .assignment(dto.getAssignment())
            .form(dto.getForm())
            .canAddSubmissions(dto.isCanAddSubmissions())
            .canEditSubmissions(dto.isCanEditSubmissions())
            .canDeleteSubmissions(dto.isCanDeleteSubmissions())
            .build();
    }

    @Named("assignmentUserFormsV1")
    public Set<AssignmentFormV1Dto> getUserForms(Assignment assignment) {
        if (!SecurityUtils.isAuthenticated()) {
            return Set.of();
        }
        final var currentUser = SecurityUtils.getCurrentUserDetailsOrThrow();
        if (assignment.getForms() == null) {
            return Set.of();
        }

        return assignment.getForms()
            .stream()
            .filter((form) -> currentUser.getUserFormsUIDs()
                .contains(form))
            .map((form) -> AssignmentFormDto.builder()
                .form(form)
                .assignment(assignment.getUid())
                .canAddSubmissions(formAccessService.canAddSubmissions(form))
                .canEditSubmissions(formAccessService.canEditSubmissions(form))
                .canDeleteSubmissions(formAccessService.canDeleteSubmissions(form))
                .build())
            .map(this::toV1Dto)
            .collect(Collectors.toSet());
    }
}

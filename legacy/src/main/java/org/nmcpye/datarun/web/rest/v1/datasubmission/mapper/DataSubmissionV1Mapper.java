package org.nmcpye.datarun.web.rest.v1.datasubmission.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.web.rest.v1.datasubmission.dto.DataSubmissionV1Dto;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DataSubmissionV1Mapper {

    @Mapping(target = "form", source = "templateUid")
    @Mapping(target = "formVersion", source = "templateVersionUid")
    @Mapping(target = "version", source = "templateVersionNo")
    @Mapping(target = "team", source = "teamUid")
    @Mapping(target = "orgUnit", source = "orgUnitUid")
    @Mapping(target = "activity", source = "activityUid")
    @Mapping(target = "assignment", source = "assignmentUid")
    DataSubmission toEntity(DataSubmissionV1Dto dto);

    @Mapping(source = "form", target = "templateUid")
    @Mapping(source = "formVersion", target = "templateVersionUid")
    @Mapping(source = "version", target = "templateVersionNo")
    @Mapping(source = "team", target = "teamUid")
    @Mapping(source = "orgUnit", target = "orgUnitUid")
    @Mapping(source = "activity", target = "activityUid")
    @Mapping(source = "assignment", target = "assignmentUid")
    DataSubmissionV1Dto toDto(DataSubmission entity);

    List<DataSubmission> toEntity(List<DataSubmissionV1Dto> dtoList);

    List<DataSubmissionV1Dto> toDto(List<DataSubmission> entityList);
}

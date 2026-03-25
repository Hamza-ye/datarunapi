package org.nmcpye.datarun.analytics.etl.mapper;

import org.mapstruct.*;
import org.nmcpye.datarun.analytics.etl.model.SubmissionContext;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DataSubmissionMapper {
    @Mapping(target = "startTime", source = "startEntryTime")
    @Mapping(target = "submissionCreationTime", source = "createdDate")
    @Mapping(target = "assignmentUid", source = "assignment")
    @Mapping(target = "activityUid", source = "activity")
    @Mapping(target = "orgUnitUid", source = "orgUnit")
    @Mapping(target = "teamUid", source = "team")
    @Mapping(target = "templateVersionUid", source = "formVersion")
    @Mapping(target = "templateUid", source = "form")
    @Mapping(target = "submissionSerial", source = "serialNumber")
    @Mapping(target = "submissionUid", source = "uid")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "lastModifiedBy", source = "lastModifiedBy")
    @Mapping(target = "submissionId", source = "id")
    @Mapping(target = "version", source = "lockVersion")
    @Mapping(target = "deletedAt", expression = "java(ds.getDeleted() != null && ds.getDeleted() == true ? java.time.Instant.now() : null)")
    SubmissionContext toDto(DataSubmission ds);

    // @InheritInverseConfiguration(name = "toEntity")
    // SubmissionContext toEntity(DataSubmission dataSubmission);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    DataSubmission partialUpdate(SubmissionContext submissionContext, @MappingTarget DataSubmission dataSubmission);
}

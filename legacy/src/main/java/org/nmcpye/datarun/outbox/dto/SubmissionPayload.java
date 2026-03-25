package org.nmcpye.datarun.outbox.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

@Data
@AllArgsConstructor
@Builder
public class SubmissionPayload implements Serializable {
    @NotNull
    private final String id;
    private final String createdBy;
    private final String lastModifiedBy;
    private final Instant createdDate;
    private final Instant lastModifiedDate;
    @NotNull(message = "serialNumber can't be null")
    private final Long serialNumber;
    @NotNull(message = "uid can't be null")
    @Size(max = 11)
    private final String uid;
    private final Instant deletedAt;
    private final JsonNode formData;
    private final FlowStatus status;
    @NotNull(message = "submission must include a template uid")
    private final String form;
    @NotNull(message = "submission must include a template version uid")
    private final String formVersion;
    private final Integer version;
    private final String team;
    private final String teamCode;
    private final String orgUnit;
    private final String orgUnitCode;
    private final String orgUnitName;
    private final String activity;
    private final String assignment;
    private final Instant startEntryTime;
    private final Instant finishedEntryTime;
}

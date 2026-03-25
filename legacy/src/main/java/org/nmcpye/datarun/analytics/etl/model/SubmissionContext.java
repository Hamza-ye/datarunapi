package org.nmcpye.datarun.analytics.etl.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

/**
 * DTO for {@link DataSubmission}
 */
@Getter
@Setter
@Builder
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionContext implements Serializable {
    private Long outboxId;
    private UUID ingestId;

    @NotNull
    @Size(max = 11)
    private String templateUid;

    @NotNull
    @Size(max = 11)
    private String templateVersionUid;

    @NotNull
    @Size(max = 11)
    private String teamUid;

    @NotNull
    private Integer version;

    @NotNull
    @Size(max = 11)
    private String orgUnitUid;

    @NotNull
    @Size(max = 11)
    private String activityUid;

    @NotNull
    @Size(max = 11)
    private String assignmentUid;

    @NotNull
    private Long submissionSerial;

    @NotNull
    @Size(max = 26)
    private String submissionId;
    @NotNull
    @Size(max = 11)
    private String submissionUid;

    private FlowStatus status;

    private Instant startTime;

    private Instant submissionCreationTime;

    private Instant deletedAt;

    @NotNull
    private String createdBy;
    private String lastModifiedBy;
}

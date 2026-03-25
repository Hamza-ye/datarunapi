package org.nmcpye.datarun.web.rest.v1.datasubmission.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

/**
 * Frozen DTO representing the v1 DataSubmission shape expected by the mobile
 * application.
 * This DTO decouples the mobile client from the internal JPA entity.
 */
@Getter
@Setter
public class DataSubmissionV1Dto {

    private String uid;

    private JsonNode formData;

    private FlowStatus status;

    @JsonProperty("form")
    private String templateUid;

    @JsonProperty("formVersion")
    private String templateVersionUid;

    @JsonProperty("version")
    private Integer templateVersionNo;

    @JsonProperty("team")
    private String teamUid;

    @JsonProperty("orgUnit")
    private String orgUnitUid;

    @JsonProperty("activity")
    private String activityUid;

    @JsonProperty("assignment")
    private String assignmentUid;

    private Instant startEntryTime;

    private Instant finishedEntryTime;
}

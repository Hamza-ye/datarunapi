package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * V2 POST request body for creating/updating a submission.
 * <p>
 * Accepts the canonical shape (§4). {@code submission_uid} is optional —
 * if absent, the server generates one (new submission).
 * If present, it's treated as an upsert.
 *
 * @author Hamza Assada
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class V2SubmissionCreateRequest {

    @JsonProperty("submission_uid")
    private String submissionUid;

    @NotNull(message = "template_uid is required")
    @JsonProperty("template_uid")
    private String templateUid;

    @JsonProperty("version_uid")
    private String versionUid;

    @JsonProperty("version_number")
    private Integer versionNumber;

    private String status;

    private String team;

    @JsonProperty("org_unit")
    private String orgUnit;

    private String activity;
    private String assignment;

    @JsonProperty("start_entry_time")
    private Instant startEntryTime;

    @JsonProperty("finished_entry_time")
    private Instant finishedEntryTime;

    /**
     * Flat field values (§4.2).
     */
    @NotNull(message = "values is required")
    private JsonNode values;

    /**
     * Identity-keyed collection maps (§4.3). Optional — null means no collections.
     */
    private JsonNode collections;
}

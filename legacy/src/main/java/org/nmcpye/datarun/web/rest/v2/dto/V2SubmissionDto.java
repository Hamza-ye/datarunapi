package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * V2 response DTO for a data submission in canonical shape (§4).
 * <p>
 * Both GET response and the shape returned after POST.
 *
 * @author Hamza Assada
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V2SubmissionDto {

    @JsonProperty("submission_uid")
    private final String submissionUid;

    @JsonProperty("template_uid")
    private final String templateUid;

    @JsonProperty("version_uid")
    private final String versionUid;

    @JsonProperty("version_number")
    private final Integer versionNumber;

    private final String status;

    private final String team;

    @JsonProperty("org_unit")
    private final String orgUnit;

    private final String activity;
    private final String assignment;

    @JsonProperty("start_entry_time")
    private final Instant startEntryTime;

    @JsonProperty("finished_entry_time")
    private final Instant finishedEntryTime;

    /**
     * Flat field values — no section wrappers (§4.2).
     */
    private final JsonNode values;

    /**
     * Identity-keyed collection maps — no arrays (§4.3).
     */
    private final JsonNode collections;
}

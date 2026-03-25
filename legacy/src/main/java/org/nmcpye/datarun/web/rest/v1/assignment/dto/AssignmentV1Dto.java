package org.nmcpye.datarun.web.rest.v1.assignment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

/**
 * Frozen DTO representing the v1 Assignment structure.
 */
@Getter
@Setter
public class AssignmentV1Dto {

    private String uid;

    private String code;

    private String name;

    @JsonProperty("activity")
    private String activityUid;

    @JsonProperty("team")
    private String teamUid;

    @JsonProperty("orgUnit")
    private String orgUnitUid;

    @JsonProperty("parent")
    private String parentUid;

    @JsonProperty("progressStatus")
    private FlowStatus status;

    private Set<String> forms = new HashSet<>();

    private Map<String, Object> allocatedResources;

    private String path;
}

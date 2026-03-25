package org.nmcpye.datarun.web.rest.v1.assignment.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

/**
 * Frozen DTO for the /assignments/forms endpoint.
 */
@Getter
@Setter
public class AssignmentWithAccessV1Dto implements Serializable {
    private String id; // This is the ULID or UID depending on legacy behavior. AssignmentWithAccessDto
                       // uses 'id'.
    private String code;
    private String activity;
    private String team;
    private String orgUnit;
    private FlowStatus progressStatus;
    private Set<AssignmentFormV1Dto> accessibleForms;
}

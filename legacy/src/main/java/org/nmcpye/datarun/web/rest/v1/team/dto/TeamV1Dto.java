package org.nmcpye.datarun.web.rest.v1.team.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.iam.team.TeamFormPermissions;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Frozen DTO representing the v1 Team structure as consumed by the mobile app.
 * Read-only contract — changes to the JPA entity do not affect this shape.
 */
@Getter
@Setter
public class TeamV1Dto {

    private String id;

    private String uid;

    private String code;

    private String name;

    private String description;

    private Boolean disabled;

    @JsonProperty("activity")
    private String activityUid;

    private Set<TeamFormPermissions> formPermissions = new HashSet<>();

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

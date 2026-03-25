package org.nmcpye.datarun.web.rest.v1.orgunit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.web.rest.v1.common.IdentifiableEntityDto;

import java.time.Instant;
import java.util.Map;

/**
 * Frozen DTO representing the v1 OrgUnit structure as consumed by the mobile
 * app.
 * Read-only contract — changes to the JPA entity do not affect this shape.
 */
@Getter
@Setter
public class OrgUnitV1Dto {

    private String id;

    private String uid;

    private String code;

    private String name;

    private String path;

    @JsonProperty("parent")
    private IdentifiableEntityDto parent;

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

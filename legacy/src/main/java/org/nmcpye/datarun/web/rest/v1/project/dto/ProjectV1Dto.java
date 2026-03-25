package org.nmcpye.datarun.web.rest.v1.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Frozen DTO representing the v1 Project structure as consumed by the mobile
 * app.
 * This is a read-only contract — changes to the JPA entity do not affect this
 * shape.
 */
@Getter
@Setter
public class ProjectV1Dto {

    private String id;

    private String uid;

    private String code;

    private String name;

    private Boolean disabled;

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

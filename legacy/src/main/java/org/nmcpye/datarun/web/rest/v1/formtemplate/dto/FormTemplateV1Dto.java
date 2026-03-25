package org.nmcpye.datarun.web.rest.v1.formtemplate.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

/**
 * Frozen DTO representing the v1 FormTemplate (DataTemplate) structure
 * as consumed by the mobile app. Read-only contract.
 */
@Getter
@Setter
public class FormTemplateV1Dto {

    private String uid;

    private String code;

    private String name;

    private String versionUid;

    private Integer versionNumber;

    private Boolean deleted;

    private String description;

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

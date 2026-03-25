package org.nmcpye.datarun.web.rest.v1.activity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.web.rest.v1.common.IdentifiableEntityDto;

import java.time.Instant;
import java.util.Map;

/**
 * Frozen DTO representing the v1 Activity structure as consumed by the mobile
 * app.
 * This is a read-only contract — changes to the JPA entity do not affect this
 * shape.
 */
@Getter
@Setter
public class ActivityV1Dto {
    @Data
    public static class ActivityProjectV1Dto {
        private String id;
        private String uid;
        private String code;
        private String name;
    }

    private String id;

    private String uid;

    private String code;

    private String name;

    private Boolean disabled;

    @JsonProperty("start_date")
    private Instant startDate;

    @JsonProperty("end_date")
    private Instant endDate;

    private IdentifiableEntityDto project;

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

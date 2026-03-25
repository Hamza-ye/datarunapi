package org.nmcpye.datarun.web.rest.v1.optionset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Frozen DTO representing the v1 OptionSet structure as consumed by the mobile
 * app.
 * Includes embedded options. Read-only contract.
 */
@Getter
@Setter
public class OptionSetV1Dto {

    private String id;

    private String uid;

    private String code;

    private String name;

    @JsonProperty("options")
    private List<OptionV1Dto> options = new ArrayList<>();

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    /**
     * Embedded Option within OptionSet.
     */
    @Getter
    @Setter
    public static class OptionV1Dto {
        private String id;
        private String uid;
        private String code;
        private String name;
        private String description;
        private Integer sortOrder;
        private Map<String, String> label;
    }
}

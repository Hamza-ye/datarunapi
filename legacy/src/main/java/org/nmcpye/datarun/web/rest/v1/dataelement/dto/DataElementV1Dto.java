package org.nmcpye.datarun.web.rest.v1.dataelement.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ReferenceType;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.web.rest.v1.common.IdentifiableEntityDto;

import java.time.Instant;
import java.util.Map;

/**
 * Frozen DTO representing the v1 DataElement structure as consumed by the
 * mobile app.
 * Read-only contract.
 */
@Getter
@Setter
public class DataElementV1Dto {

    private String id;

    private String uid;

    private String code;

    private String name;

    private String shortName;

    private String description;

    @JsonProperty("type")
    private ValueType valueType;

    @JsonProperty("optionSet")
    private IdentifiableEntityDto optionSet;

    private ReferenceType resourceType;

    // extracted from translations
    private Map<String, String> label;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;
}

package org.nmcpye.datarun.analytics.etl.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Hamza Assada
 * @since 10/02/2026
 */
@Data
@Builder
public class TallCanonicalValue {
    private String canonicalElementId; // UUID string
    private String elementPath;
    private String repeatInstanceId; // nullable
    private String parentInstanceId; // nullable
    private Integer repeatIndex; // nullable// fallback if repeatInstanceId absent
    private String valueText;
    private BigDecimal valueNumber;
    private Boolean valueBool;
    private String valueJson;
    private String valueRefUid;
    private String valueRefType;
}

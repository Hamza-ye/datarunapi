package org.nmcpye.datarun.analytics.etl.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO representing a single canonical tall row to be upserted into tall_canonical.
 * Used as the batch unit for the JDBC repository.
 */
@Data
@Builder
public class TallCanonicalRow {
    private Long outboxId;
    private UUID ingestId;
    private String orgUnit;
    private String team;
    private String activity;
    private String assignment;
    private Long submissionSerialNumber;
    private String submissionId;
    private String submissionUid;
    private String templateUid;
    private String templateVersionUid;
    private Instant submissionCreationTime;
    private Instant submissionStartTime;
    private String createdBy;
    private String lastModifiedBy;

    private String canonicalElementId; // UUID string
    private String elementPath;
    private String repeatInstanceId; // nullable
    private String parentInstanceId; // nullable
    private Integer repeatIndex;
    private String valueText;
    private BigDecimal valueNumber;
    private Boolean valueBool;
    private String valueJson;
    private String valueRefUid;
    private String valueRefType;
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isDeleted;
}

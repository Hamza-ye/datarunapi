package org.nmcpye.datarun.analytics.etl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.nmcpye.datarun.analytics.etl.model.TallCanonicalRow;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * * events is the instance-level identity table (one row per instance_key) that represents
 * both submission-root and repeat instances, and it stores anchors and instance-scoped resolved refs.
 * They overlap on submission-root rows by design — different purpose, different granularity:
 * Use it when you need to reason about repeat instances, sibling attributes, anchors, or do per-instance deduping/pivots.
 *
 * @see SubmissionKeyDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class EventDto {
    private String eventId;
    private String eventType;
    private String submissionId;
    private String submissionUid;
    /// submission version
    private Long submissionSerial;


    private String eventCeId;
    private String parentEventId;

    // context
    private String assignmentUid;
    private String activityUid;
    private String orgUnitUid;
    private String teamUid;
    private String templateUid;

    /// created at server
    private Instant submissionCreationTime;
    /// created time at client
    private Instant startTime;
//    private Instant lastSeen;

//    // anchor fields
//    private String anchorCeId;
//    private String anchorRefUid;
//    private String anchorValueText;
//    private String anchorValueRefType;
//    private BigDecimal anchorConfidence;
//    private Instant anchorResolvedAt;

    /// submission created by
    private String createdBy;
    /// submission created by
    private String lastModifiedBy;
    /// event creation date
    private Instant createdAt;

    /// event update date
    private Instant updatedAt;

    /// event deletion time, null if not deleted
    private Instant deletedAt;

    @Builder.Default
    private List<TallCanonicalRow> tallCanonicalRows = new ArrayList<>();

    public static final RowMapper<EventDto> ROW_MAPPER = (rs, rowNum) -> EventDto.builder()
        .eventId(rs.getString("event_id"))
        .eventType(rs.getString("event_type"))
        .submissionUid(rs.getString("submission_uid"))
        .submissionId(rs.getString("submission_id"))

        .submissionSerial(rs.getLong("submission_serial"))
        .parentEventId(rs.getString("parent_event_id"))
        .eventCeId(rs.getString("event_ce_id"))

        .assignmentUid(rs.getString("assignment_uid"))
        .activityUid(rs.getString("activity_uid"))
        .orgUnitUid(rs.getString("org_unit_uid"))
        .teamUid(rs.getString("team_uid"))
        .createdBy(rs.getString("created_by"))
        .lastModifiedBy(rs.getString("last_modified_by"))
        .templateUid(rs.getString("template_uid"))
        .submissionCreationTime(getInstant(rs, "submission_creation_time"))
        .startTime(getInstant(rs, "start_time"))
//        .lastSeen(getInstant(rs, "last_seen"))

        // anchor fields
//        .anchorCeId(rs.getString("anchor_ce_id"))
//        .anchorRefUid(rs.getString("anchor_ref_uid"))
//        .anchorValueText(rs.getString("anchor_value_text"))
//        .anchorConfidence(rs.getBigDecimal("anchor_confidence"))
//        .anchorResolvedAt(getInstant(rs, "anchor_resolved_at"))
//        .anchorValueRefType(rs.getString("anchor_value_ref_type"))

        .createdAt(getInstant(rs, "created_at")).updatedAt(getInstant(rs, "updated_at"))
        .deletedAt(getInstant(rs, "deleted_at")).build();

    private static Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}

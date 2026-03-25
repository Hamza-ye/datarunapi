package org.nmcpye.datarun.analytics.etl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * * submission-level lookup (one row per submission) optimized for cheap grouping/filters by submission globals:
 * Use it when you want counts or group-bys at the submission level (e.g., submissions per team/week).
 * <p>
 *
 * @see EventDto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionKeyDto {
    private String submissionId;
    private Long submissionSerial;
    private String status;
    private String submissionUid;
    private String assignmentUid;
    private String activityUid;
    private String orgUnitUid;
    private String teamUid;
    private String templateUid;
    private Instant lastSeen;
    private Instant createdAt;
    private Instant updatedAt;

    public static final RowMapper<SubmissionKeyDto> ROW_MAPPER = (rs, rowNum) -> SubmissionKeyDto.builder()
        .submissionUid(rs.getString("submission_uid"))
        .submissionUid(rs.getString("submission_serial"))
        .submissionUid(rs.getString("status"))
        .submissionId(rs.getString("submission_id"))
        .assignmentUid(rs.getString("assignment_uid"))
        .activityUid(rs.getString("activity_uid"))
        .orgUnitUid(rs.getString("org_unit_uid"))
        .teamUid(rs.getString("team_uid"))
        .templateUid(rs.getString("template_uid"))
        .lastSeen(getInstant(rs, "last_seen"))
        .createdAt(getInstant(rs, "created_at"))
        .updatedAt(getInstant(rs, "updated_at"))
        .build();

    private static Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}

package org.nmcpye.datarun.analytics.etl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/// - Authoritative mapping log — stores every attempt to resolve a raw token: raw_value, raw_source, ref_type,
/// resolved_uid (11-char), confidence, resolved_at, replaced_by, notes.
///
/// - Single source for resolution decisions — every automated deterministic resolution (and misses) is recorded
/// so behavior is repeatable and auditable.
/// 1. **History & auditability** — see when a token was first resolved, how it changed, and who/what replaced it.
///  `submission_keys` or `events` are not for this.
/// 2. **Deterministic behaviour** — resolution is deterministic because the service consults the table first;
/// changing resolution is a deliberate append (and `replaced_by`) — not a silent immediate rewrite.
/// 3. **Operational control** — you can correct mappings manually (insert a new row marking `replaced_by`) and
/// the system will honor the new mapping going forward while preserving past mappings.
/// 4. **Separation of concerns** — keeps token-resolution logic decoupled from dimensional joins and event rows;
/// easier to test, backfill, or replace heuristics later.
/// 5. **Analytics on mapping quality** — you can count misses, low-confidence resolutions, and prioritize cleanup;
///  useful for data quality dashboards.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefResolutionDto {
    private String refResolutionUid;
    private String rawValue;
    private String rawSource;
    private String refType;
    private String resolvedUid;
    private BigDecimal confidence;
    private String replacedBy;
    private String notes;
    private Instant resolvedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static final RowMapper<RefResolutionDto> ROW_MAPPER = (rs, rowNum) -> RefResolutionDto.builder()
        .refResolutionUid(rs.getString("value_ref_uid"))
        .rawValue(rs.getString("raw_value"))
        .rawSource(rs.getString("raw_source"))
        .refType(rs.getString("ref_type"))
        .resolvedUid(rs.getString("resolved_uid"))
        .confidence(rs.getBigDecimal("confidence"))
        .resolvedAt(getInstant(rs, "resolved_at"))
        .replacedBy(rs.getString("replaced_by"))
        .notes(rs.getString("notes"))
        .createdAt(getInstant(rs, "created_at"))
        .updatedAt(getInstant(rs, "updated_at"))
        .build();

    private static Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}

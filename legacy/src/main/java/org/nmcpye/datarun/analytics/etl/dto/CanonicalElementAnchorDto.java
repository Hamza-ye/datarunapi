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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanonicalElementAnchorDto {
    private String canonicalElementId;
    private Boolean anchorAllowed;
    private Integer anchorPriority;
    private String updatedBy;
    private Instant updatedAt;

    public static final RowMapper<CanonicalElementAnchorDto> ROW_MAPPER = (rs, rowNum) -> CanonicalElementAnchorDto.builder()
        .canonicalElementId(rs.getString("canonical_element_id"))
        .anchorAllowed(rs.getObject("anchor_allowed") == null ? Boolean.FALSE : rs.getBoolean("anchor_allowed"))
        .anchorPriority(rs.getObject("anchor_priority") == null ? 100 : rs.getInt("anchor_priority"))
        .updatedBy(rs.getString("updated_by"))
        .updatedAt(getInstant(rs, "updated_at"))
        .build();

    private static Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}

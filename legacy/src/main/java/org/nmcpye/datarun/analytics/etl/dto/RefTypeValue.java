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
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefTypeValue {
    private String templateUid;
    private String submissionUid;
    private String instanceKey;
    private UUID ceId;
    private String refType;
    private String rawValue;
    private String valueRefUid;
    private String optionSetUid;
    private Instant createdAt;
    private Instant updatedAt;

    public static final RowMapper<RefTypeValue> ROW_MAPPER = (rs, rowNum) -> RefTypeValue.builder()
        .templateUid(rs.getString("template_uid"))
        .submissionUid(rs.getString("submission_uid"))
        .instanceKey(rs.getString("instance_key"))
        .ceId(rs.getString("ce_id") != null ? UUID.fromString(rs.getString("ce_id")) : null)
        .refType(rs.getString("ref_type"))
        .rawValue(rs.getString("raw_value"))
        .valueRefUid(rs.getString("value_ref_uid"))
        .optionSetUid(rs.getString("option_set_uid"))
        .createdAt(getInstant(rs, "created_at"))
        .updatedAt(getInstant(rs, "updated_at"))
        .build();

    private static Instant getInstant(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts != null ? ts.toInstant() : null;
    }
}


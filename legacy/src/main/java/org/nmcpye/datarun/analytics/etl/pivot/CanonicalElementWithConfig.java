package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CanonicalElementWithConfig {
    // base CE
    private String canonicalElementId;
    private String templateUid;
    private String safeName;
    private String dataType;
    private String semanticType;
    private String parentRepeatId;
    // config (cec)
    private String safeNameOverride;
    private boolean explode;             // explode multiselect arrays into event_multiselect

    public boolean isMultiSelect() {
        return dataType.equals("MultiSelectOption");
    }

    public final static RowMapper<CanonicalElementWithConfig> MAPPER = (rs, rowNum) ->
        CanonicalElementWithConfig.builder()
            .canonicalElementId(rs.getString("id"))
            .templateUid(rs.getString("template_uid"))
            .safeName(rs.getString("safe_name"))
            .dataType(rs.getString("data_type"))
            .semanticType(rs.getString("semantic_type"))
            .parentRepeatId(rs.getString("parent_repeat_id"))
            .safeNameOverride(rs.getString("safe_name_override"))
            .explode(rs.getBoolean("explode"))
            .build();
}

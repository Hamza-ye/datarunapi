package org.nmcpye.datarun.analytics.etl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.nmcpye.datarun.analytics.etl.model.TallCanonicalRow;
import org.springframework.jdbc.core.RowMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hamza Assada
 * @since 10/02/2026
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class EventRow {
    private String eventId;
    private String eventType;
    private Integer eventIndex;

    private String eventCeId;
    private String parentEventId;

    @Builder.Default
    private List<TallCanonicalRow> tallCanonicalRows = new ArrayList<>();

    public static final RowMapper<EventRow> ROW_MAPPER = (rs, rowNum) -> EventRow.builder()
            .eventId(rs.getString("event_id"))
            .eventType(rs.getString("event_type"))
            .parentEventId(rs.getString("parent_event_id"))
            .eventCeId(rs.getString("event_ce_id")).build();
}

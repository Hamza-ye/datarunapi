package org.nmcpye.datarun.analytics.etl.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author Hamza Assada
 * @since 10/02/2026
 */
@Getter
@Setter
@Builder
@Accessors(chain = true, fluent = true)
@AllArgsConstructor
public class FlattenSubmission {
    @Singular
    private List<EventRow> eventRows;

    @Singular
    private List<TallCanonicalValue> tallCanonicalRows;
}

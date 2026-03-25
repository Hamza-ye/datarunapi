package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * An effect produced by a {@link V2Rule} when its condition is true.
 *
 * @author Hamza Assada
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V2RuleEffect {

    /**
     * The {@code node_id} of the field this effect targets.
     */
    private final String targetNode;

    /**
     * The action: SHOW, HIDE, SET_REQUIRED, ASSIGN, ERROR, WARNING, etc.
     */
    private final String action;

    /**
     * Optional assigned value (for ASSIGN actions).
     */
    private final Object value;

    /**
     * Optional message (for ERROR/WARNING actions).
     */
    private final Object message;
}

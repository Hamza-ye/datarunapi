package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * A V2 rule in JsonLogic AST format.
 * <p>
 * Replaces V1's {@code DataFieldRule} which used template-string expressions
 * like {@code #{gender} == 'FEMALE'}.
 *
 * @author Hamza Assada
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class V2Rule {

    /**
     * Generated rule identifier: "rule_{ownerNodeId}_{index}".
     */
    private final String ruleId;

    /**
     * The section scope where this rule's owner lives.
     */
    private final String scope;

    /**
     * Data paths that trigger re-evaluation.
     * e.g. ["values.gender"] or ["collections.medicines.amd"]
     */
    private final List<String> triggers;

    /**
     * JsonLogic AST condition. Represented as a Map/List structure
     * that serializes directly to the JsonLogic JSON format.
     * <p>
     * Example: {@code {"==": [{"var": "values.gender"}, "FEMALE"]}}
     */
    private final Object condition;

    /**
     * Effects to apply when the condition evaluates to true.
     */
    private final List<V2RuleEffect> effects;
}

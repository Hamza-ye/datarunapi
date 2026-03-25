package org.nmcpye.datarun.web.rest.v2.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * A single node in the V2 template tree.
 * <p>
 * Sections become container nodes (type = "section" or "repeater").
 * Fields become leaf nodes (type = the field's ValueType name, e.g.
 * "SelectOne").
 *
 * @author Hamza Assada
 */
@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateTreeNode {

    /**
     * Unique identifier within the tree.
     * For fields: {@code FormDataElementConf.id} (element UID).
     * For sections: {@code FormSectionConf.name}.
     */
    private final String nodeId;

    /**
     * Determines the UI widget or container.
     * "section", "repeater", or a field type like "SelectOne", "Text", "Date", etc.
     */
    private final String type;

    /**
     * The canonical data-binding key.
     * For fields: the field {@code name} (key in {@code values} or collection-row).
     * For repeaters: the section {@code name} (key in {@code collections}).
     * For non-repeater sections: null (sections don't appear in data).
     */
    private final String binding;

    private final Map<String, String> label;
    private final Boolean mandatory;
    private final String optionSet;
    private final String description;
    private final Object validation;

    /**
     * V2 rules — JsonLogic AST format. Never V1 expression strings.
     */
    private final List<V2Rule> rules;

    /**
     * Child nodes, ordered by {@code order}. Empty for leaf fields.
     */
    private final List<TemplateTreeNode> children;
}

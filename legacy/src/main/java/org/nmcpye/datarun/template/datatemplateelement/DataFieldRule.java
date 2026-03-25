package org.nmcpye.datarun.template.datatemplateelement;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.RuleAction;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * A DataFieldRule.
 */
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataFieldRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull
    private String expression;

    @NotNull
    private RuleAction action;

    private Map<String, String> message;

    private String assignedValue;

    public void setMessage(Map<String, String> message) {
        this.message = action == RuleAction.Error ? Objects.requireNonNullElseGet(message, () -> Map.of("en", "Field with error, check")) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataFieldRule that = (DataFieldRule) o;
        return Objects.equals(expression, that.expression) && action == that.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, action);
    }
}

package org.nmcpye.datarun.template.datatemplateelement.datafield;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class DefaultField extends AbstractField {
    private Object defaultValue;
    private boolean mandatory;
    private boolean readOnly;
    private List<String> appearance;
    private String constraint;
    private Map<String, String> constraintMessage;
    private boolean mainField;

    public Map<String, String> getConstraintMessage() {
        return constraint != null
                ? Objects.requireNonNullElseGet(this.constraintMessage, () -> Map.of("en", "Field with error, check"))
                : null;
    }
}

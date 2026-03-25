package org.nmcpye.datarun.template.datatemplateelement.datafield;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.DataFieldRule;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(value = {"path"}, allowGetters = true)
@Getter
@Setter
public class AbstractField implements Serializable {
    public static final String PATH_SEP = ".";
    @Serial
    private static final long serialVersionUID = 1L;
    @NotNull
    private String name;
    @Size(max = 2000)
    private String description;
    private Map<String, String> label;
    @NotNull
    private ValueType type;
    private List<DataFieldRule> rules = new ArrayList<>();
    private String path;
    private String calculation;
    private Integer order;

    public void setLabel(Map<String, String> label) {
        this.label = Objects.requireNonNullElseGet(label, () -> Map.of("en", this.name));
    }

    public Map<String, String> getLabel() {
        if (this.label == null) {
            return Map.of("en", this.name);
        }
        return label;
    }

    public AbstractField rules(List<DataFieldRule> dataFieldRules) {
        this.setRules(dataFieldRules);
        return this;
    }

    public AbstractField description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setOrder(Integer order) {
        this.order = Objects.requireNonNullElseGet(order, () -> 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractField that = (AbstractField) o;
        return path != null && path.equals(that.path) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path != null ? path.hashCode() : 0);
    }

    /**
     * Determines if the given field is of type ResourceField.
     * This static method checks whether the provided AbstractField instance
     * is specifically an instance of the ResourceField class.
     *
     * @param field The AbstractField instance to be checked.
     * @return true if the field is an instance of ResourceField, false otherwise.
     */
    public static boolean isResourceTypeField(AbstractField field) {
        return field instanceof ReferenceField;
    }

    @Override
    public String toString() {
        return "AbstractField{" +
            "name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", label=" + label +
            ", rules=" + rules +
            ", path='" + path + '\'' +
            ", order=" + order +
            '}';
    }
}

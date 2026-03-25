package org.nmcpye.datarun.template.datatemplateelement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // tolerate extra fields in DB
@JsonInclude(JsonInclude.Include.NON_NULL)  // omit nulls when serializing
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public abstract class AbstractElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String parent;
    @NotNull
    private String name;
    private String path;
    private String code;
    @Size(max = 2000)
    private String description;
    private Integer order;
    private Map<String, String> label;
    private List<DataFieldRule> rules = new ArrayList<>();
    private Map<String, Object> properties;

//    public abstract String getId();

    public AbstractElement path(String path) {
        setPath(path);
        return this;
    }

    /**
     * only {@link FieldTemplateElementDto} can define a type
     * defined here to provide a uniform iteration over elements without needing knowledge about certain type
     *
     * @return {@link FieldTemplateElementDto#getType()} or null for {@link SectionTemplateElementDto}
     */
    public ValueType getType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SectionTemplateElementDto that)) return false;
        return Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}

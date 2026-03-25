package org.nmcpye.datarun.template.datatemplate.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * DTO, a merge of {@link DataTemplate}
 */
@SuppressWarnings("unused")
@Setter
@Getter
@AllArgsConstructor
public class DataTemplateInstanceDto implements DataTemplateVersionInterface {
    @Size(max = 26)
    String id;
    String code;
    /**
     * output the master {@link DataTemplate} uid
     */
    @Size(max = 11)
    private String uid;

    /**
     * formVersion is treated as output-only (i.e. set in backend and ignored on inbound JSON)
     */
    @JsonProperty(access = READ_ONLY)
    private String versionUid;

    /**
     * currentVersion is treated as output-only (i.e. set in backend and ignored on inbound JSON)
     */
    @JsonProperty(access = READ_ONLY)
    private Integer versionNumber;

    // metadata (form Template)
    @NotNull
    @NotEmpty(message = "name cannot be empty")
    private String name;

    @Size(max = 2000)
    private String description;
    private Boolean deleted;
    private Map<String, String> label;

    /**
     * the only versioned parts of a form template are fields, and sections (form version)
     */
    private List<FieldTemplateElementDto> fields;
    private List<SectionTemplateElementDto> sections;

    @JsonIgnore
    public String getId() {
        return id;
    }

    @Override
    public DataTemplateInstanceDto sections(List<SectionTemplateElementDto> sections) {
        this.setSections(sections);
        return this;
    }

    @Override
    public DataTemplateInstanceDto fields(List<FieldTemplateElementDto> fields) {
        this.setFields(fields);
        return this;
    }


    /**
     * @return mapping of repeatPath -> (relativeChildPath -> elementId). elementId is the last segment of a path, its either a uid for FormFields,
     * or a name for Sections fields, and they are unique across the form
     */
    @JsonIgnore
    public Map<String, Map<String, String>> getRepeatSectionChildrenMap() {
        if (sections == null) {
            return Map.of();
        }
        return sections.stream()
            .filter(SectionTemplateElementDto::getRepeatable)
            .collect(Collectors.toMap(AbstractElement::getPath, (s) ->
                getChildrenRelativePathMap(s.getPath())));
    }

    /**
     * @param path parent path
     * @return list of children of certain path
     */
    @JsonIgnore
    public List<FieldTemplateElementDto> getChildrenOfPath(String path) {
        if (fields == null) {
            return List.of();
        }
        return fields.stream()
            .filter(f -> f.getPath().startsWith(path))
            .collect(Collectors.toList());
    }

    /**
     * @return element path → element map for quick get by path
     */
    @JsonIgnore
    public Map<String, AbstractElement> getAllElementPathMap() {
        List<AbstractElement> fieldElements = fields != null ? new java.util.ArrayList<>(fields) : List.of();
        List<AbstractElement> sectionElements = sections != null ? new java.util.ArrayList<>(sections) : List.of();
        return Stream.concat(fieldElements.stream(), sectionElements.stream())
            .collect(Collectors.toMap(AbstractElement::getPath, Function.identity()));
    }

    /**
     * @param path an ancestor Path
     * @return path → childPath relative to the ancestor
     */
    @JsonIgnore
    public Map<String, String> getChildrenRelativePathMap(String path) {
        if (fields == null) {
            return Map.of();
        }

        // map child fields: find fields whose path startWith(repeatPath + ".")
        return getFields().stream()
            .filter(f -> f.getPath() != null && f.getPath().startsWith(path + "."))
            .collect(Collectors.toMap(
                f -> f.getPath().substring((path + ".").length()), // relative path
                FieldTemplateElementDto::getId
            ));
    }

    /**
     * @return repeatPath → categoryElementId so ETL can set category_id easily
     */
    @JsonIgnore
    public Map<String, String> getRepeatCategoryElementMap() {
        if (sections == null) {
            return Map.of();
        }
        return sections.stream()
            .filter(SectionTemplateElementDto::getRepeatable)
            .filter(r -> r.getCategoryId() != null)
            .collect(Collectors.toMap(AbstractElement::getPath,
                SectionTemplateElementDto::getCategoryId));
    }


    /**
     * @return materialized repeatable paths, e.g. "adult.adultClassification"
     */
    @JsonIgnore
    public List<String> getRepeatSectionsPaths() {
        if (sections == null) return List.of();
        return sections.stream()
            .filter(s -> Boolean.TRUE.equals(s.getRepeatable()))
            .map(SectionTemplateElementDto::getPath)
            .collect(Collectors.toList());
    }

    /**
     * @return top-level fields mapping: relativePath -> elementId
     */
    @JsonIgnore
    public Map<String, String> getTopLevelFieldPathToElementId() {
        if (fields == null) return Map.of();
        List<SectionTemplateElementDto> sectionList = sections == null ? List.of() : sections;
        return getFields().stream()
            .filter(f -> {
                String p = f.getPath();
                return p != null && sectionList.stream().noneMatch(s -> p.startsWith(s.getPath() + "."));
            })
            .collect(Collectors.toMap(FieldTemplateElementDto::getPath, FieldTemplateElementDto::getId));
    }
}

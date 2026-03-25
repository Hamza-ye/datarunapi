package org.nmcpye.datarun.template.datatemplateelement.datafield;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Section extends DefaultField {
    @JsonDeserialize(contentUsing = FieldDeserializer.class)
    private List<AbstractField> fields = new LinkedList<>();

    public List<AbstractField> getFields() {
        return fields;
    }

    public void setFields(List<AbstractField> fields) {
        this.fields = fields;
    }

    /**
     * Flattens the hierarchical structure of fields within this section and its subsections.
     * This method recursively traverses through all fields, including nested sections,
     * and creates a flat list of all fields.
     *
     * @return A List of AbstractField objects representing all fields in this section
     *         and its subsections in a flattened structure. The list maintains the order
     *         of fields as they appear in the hierarchical structure.
     */
    public List<AbstractField> flattenFields() {
        List<AbstractField> flatList = new ArrayList<>();
        for (AbstractField field : this.fields) {
            flatList.add(field);
            Section section = ((Section) field);
            if (section.getFields() != null && !section.getFields().isEmpty()) {
                flatList.addAll(section.flattenFields());
            }
        }
        return flatList;
    }
}

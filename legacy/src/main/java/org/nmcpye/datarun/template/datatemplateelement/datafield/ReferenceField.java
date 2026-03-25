package org.nmcpye.datarun.template.datatemplateelement.datafield;

import jakarta.validation.constraints.NotNull;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ReferenceType;

import java.util.List;
import java.util.Objects;

public class ReferenceField extends DefaultField {

    // TODO rename to referenceType
    @NotNull
    private ReferenceType resourceType;

    // TODO rename to referenceMetadataSchema
    @NotNull
    private String resourceMetadataSchema;

    private List<String> displayAttributes;

    private List<AllowedAction> allowedActions;

    public ReferenceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ReferenceType resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceMetadataSchema() {
        return resourceMetadataSchema;
    }

    public void setResourceMetadataSchema(String resourceMetadataSchema) {
        this.resourceMetadataSchema = resourceMetadataSchema;
    }

    public List<String> getDisplayAttributes() {
        return displayAttributes;
    }

    public void setDisplayAttributes(List<String> displayAttributes) {
        this.displayAttributes = displayAttributes;
    }

    public List<AllowedAction> getAllowedActions() {
        return allowedActions;
    }

    public void setAllowedActions(List<AllowedAction> allowedActions) {
        this.allowedActions = allowedActions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReferenceField that)) return false;
        if (!super.equals(o)) return false;
        return resourceType == that.resourceType && Objects.equals(resourceMetadataSchema, that.resourceMetadataSchema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resourceType, resourceMetadataSchema);
    }
}

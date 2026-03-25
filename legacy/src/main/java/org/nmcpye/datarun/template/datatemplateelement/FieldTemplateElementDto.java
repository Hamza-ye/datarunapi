package org.nmcpye.datarun.template.datatemplateelement;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import org.nmcpye.datarun.template.datatemplateelement.enumeration.ReferenceType;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.sharedkernal.enumeration.ValueTypeRendering;
import org.nmcpye.datarun.template.datatemplateelement.datafield.ScannedCodeProperties;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
public class FieldTemplateElementDto extends AbstractElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private ValueType type;
    private Object defaultValue;
    private Boolean mandatory = Boolean.FALSE;
    private Boolean showInSummary = Boolean.FALSE;

    private String optionSet;
    private String choiceFilter;
    private ElementValidationRule validationRule;

    private Boolean mainField;

    /**
     * Deprecated, use {@link #validationRule#expression}'sinstead
     */
    @Deprecated(since = "V7", forRemoval = true)
    private String constraint;
    /**
     * Deprecated, use {@link #validationRule#validationMessage} instead
     */
    @Deprecated(since = "V7", forRemoval = true)
    private Map<String, String> constraintMessage;

    private Boolean gs1Enabled;

    private ScannedCodeProperties scannedCodeProperties;
    /**
     * resourceType for ReferenceField type
     */
    private ReferenceType resourceType;
    /**
     * resourceMetadataSchema for ReferenceField type
     */
    private String resourceMetadataSchema;
    private ValueTypeRendering valueTypeRendering = ValueTypeRendering.DEFAULT;
    private AggregationType aggregationType = AggregationType.DEFAULT;
    private Boolean isMeasure = Boolean.TRUE;
    private Boolean isDimension = Boolean.FALSE;

    public Boolean isMultiSelect() {
        return this.type != null && this.type.isOptionsType() ? this.type == ValueType.SelectMulti : null;
    }


    // --- Legacy compatibility for old 'constraint' and 'constraintMessage' ---

    /**
     * Legacy: constraint (single string). Map into validationRule.validationMessage (pick a default locale if needed).
     * Accept legacy input but DO NOT emit it (no @JsonGetter).
     */
    @Deprecated(since = "V7", forRemoval = true)
    @JsonSetter("constraint")
    public void setConstraintLegacy(String legacyConstraint) {
        if (legacyConstraint == null) return;
        if (this.validationRule == null) {
            this.validationRule = new ElementValidationRule();
        }
        // Put legacy constraint into the validationMessage map; choose a default key e.g. "default"
        Map<String, String> vm = this.validationRule.getValidationMessage();
        if (vm == null) {
            vm = new LinkedHashMap<>();
            this.validationRule.setValidationMessage(vm);
        }
        // only set if not already present (prefer newer validationRule)
        vm.putIfAbsent("default", legacyConstraint);
    }

    /**
     * Legacy: constraintMessage (map of locale->message). Map into validationRule.validationMessage.
     */
    @Deprecated(since = "V7", forRemoval = true)
    @JsonSetter("constraintMessage")
    public void setConstraintMessageLegacy(Map<String, String> legacyConstraintMessage) {
        if (legacyConstraintMessage == null || legacyConstraintMessage.isEmpty()) return;
        if (this.validationRule == null) {
            this.validationRule = new ElementValidationRule();
        }

        Map<String, String> vm = this.validationRule.getValidationMessage();
        if (vm == null) {
            vm = new LinkedHashMap<>();
            this.validationRule.setValidationMessage(vm);
        }
        // merge legacy map, but do not overwrite existing keys
        legacyConstraintMessage.forEach(vm::putIfAbsent);
    }

    // Optionally expose the old getters as read-only and deprecated if you really must emit them:
    // @Deprecated
    // @JsonGetter("constraint")
    // public String getConstraintLegacy() { ... } // map from validationRule as needed

    // Accept legacy property when reading JSON:
    // Accept legacy JSON input "mainField" but do not emit it when serializing:
    /**
     * deprecated property use {@link #showInSummary} instead
     *
     * @return show in summary or not
     */
    @Deprecated(forRemoval = true)
    @JsonSetter("mainField")
    public void setMainFieldLegacy(Boolean legacyMain) {
        if (legacyMain != null) this.showInSummary = legacyMain;
    }

    @Deprecated(forRemoval = true)
    @JsonGetter("readOnly")
    public Boolean getReadOnlyLegacy() {
        return false;
    }

    // If you want to emit it (not recommended), add:
    // Optionally expose replacement property for older clients when serializing:
    // @JsonGetter("mainField")
    // public Boolean getMainFieldLegacy() { return this.showInSummary; }

    @Override
    public FieldTemplateElementDto path(String path) {
        this.setPath(path);
        return this;
    }

    public FieldTemplateElementDto type(ValueType type) {
        this.setType(type);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldTemplateElementDto that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

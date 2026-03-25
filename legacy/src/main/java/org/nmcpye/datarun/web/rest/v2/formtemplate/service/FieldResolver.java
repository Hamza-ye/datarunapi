package org.nmcpye.datarun.web.rest.v2.formtemplate.service;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Resolves V1 {@code #{fieldName}} references to their V2 namespace
 * using the same hierarchical scope chain the mobile app uses:
 * same section → parent → parent of parent → root → adjacent (first match).
 * <p>
 * Built once per template transformation, then queried per rule reference.
 * <p>
 * This class has no Spring dependencies and no side effects.
 *
 * @author Hamza Assada
 */
public final class FieldResolver {

    private static final Logger log = LoggerFactory.getLogger(FieldResolver.class);

    /**
     * Result of resolving a V1 field reference.
     */
    public record ResolvedField(
            String fieldName,
            String ownerSection,
            boolean ownerIsRepeater) {
        /**
         * @return the V2 var path for use in JsonLogic AST
         */
        public String toV2Var() {
            // If owner is a repeater, the field is inside a collection row → _row.fieldName
            // If owner is a non-repeater section, the field is in flat values →
            // values.fieldName
            return ownerIsRepeater ? "_row." + fieldName : "values." + fieldName;
        }

        /**
         * @return the trigger path for V2Rule.triggers
         */
        public String toTriggerPath() {
            return ownerIsRepeater
                    ? "collections." + ownerSection + "." + fieldName
                    : "values." + fieldName;
        }
    }

    // fieldName → owning section name
    private final Map<String, String> fieldOwnerMap;

    // sectionName → parent section name (null if root-level)
    private final Map<String, String> sectionParentMap;

    // sectionName → true if repeatable
    private final Map<String, Boolean> sectionRepeatableMap;

    private FieldResolver(Map<String, String> fieldOwnerMap,
            Map<String, String> sectionParentMap,
            Map<String, Boolean> sectionRepeatableMap) {
        this.fieldOwnerMap = fieldOwnerMap;
        this.sectionParentMap = sectionParentMap;
        this.sectionRepeatableMap = sectionRepeatableMap;
    }

    /**
     * Build a FieldResolver from the template's sections and fields.
     */
    public static FieldResolver build(List<SectionTemplateElementDto> sections,
            List<FieldTemplateElementDto> fields) {
        Map<String, String> fieldOwnerMap = new HashMap<>();
        Map<String, String> sectionParentMap = new HashMap<>();
        Map<String, Boolean> sectionRepeatableMap = new HashMap<>();

        if (sections != null) {
            for (SectionTemplateElementDto section : sections) {
                sectionParentMap.put(section.getName(), section.getParent());
                sectionRepeatableMap.put(
                        section.getName(),
                        Boolean.TRUE.equals(section.getRepeatable()));
            }
        }

        if (fields != null) {
            for (FieldTemplateElementDto field : fields) {
                if (field.getName() != null && field.getParent() != null) {
                    fieldOwnerMap.put(field.getName(), field.getParent());
                }
            }
        }

        return new FieldResolver(
                Collections.unmodifiableMap(fieldOwnerMap),
                Collections.unmodifiableMap(sectionParentMap),
                Collections.unmodifiableMap(sectionRepeatableMap));
    }

    /**
     * Resolve a V1 {@code #{fieldName}} reference from the given scope context.
     * <p>
     * Walk order: fromSection → parent → parent of parent → root → adjacent (first
     * match).
     *
     * @param fieldName       the field name referenced in the V1 expression
     * @param fromSectionName the section where the rule owner lives
     * @return resolved field info, or empty if the field could not be found
     */
    public Optional<ResolvedField> resolve(String fieldName, String fromSectionName) {
        if (fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }

        String ownerSection = fieldOwnerMap.get(fieldName);
        if (ownerSection == null) {
            log.warn("Field '{}' referenced in rule but not found in any section", fieldName);
            return Optional.empty();
        }

        // Walk the hierarchy: check if ownerSection is reachable from fromSectionName
        // Step 1-4: Walk from fromSectionName up the parent chain
        String current = fromSectionName;
        while (current != null) {
            if (current.equals(ownerSection)) {
                return Optional.of(new ResolvedField(
                        fieldName, ownerSection, isRepeater(ownerSection)));
            }
            current = sectionParentMap.get(current);
        }

        // Step 5: Adjacent section (field exists but not in the hierarchy chain)
        // The field was found in fieldOwnerMap, it's just in a different branch
        log.debug("Field '{}' resolved to adjacent section '{}' (not in parent chain of '{}')",
                fieldName, ownerSection, fromSectionName);
        return Optional.of(new ResolvedField(
                fieldName, ownerSection, isRepeater(ownerSection)));
    }

    /**
     * Check if a section is a repeater.
     */
    public boolean isRepeater(String sectionName) {
        return Boolean.TRUE.equals(sectionRepeatableMap.get(sectionName));
    }
}

package org.nmcpye.datarun.web.rest.v2.formtemplate.service;

import org.nmcpye.datarun.template.datatemplateelement.ElementValidationRule;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.web.rest.v2.dto.TemplateTreeNode;
import org.nmcpye.datarun.web.rest.v2.dto.V2Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Transforms flat V1 {@code sections[]} + {@code fields[]} into a nested V2
 * template tree.
 * <p>
 * Implements the §5.3 HashMap Registry Algorithm from the V2 contract doc.
 * O(N) time, O(N) space. No DB queries.
 * <p>
 * This class has no Spring dependencies, no state, and no side effects.
 *
 * @author Hamza Assada
 */
public final class TemplateTreeTransformer {

        private static final Logger log = LoggerFactory.getLogger(TemplateTreeTransformer.class);

        private TemplateTreeTransformer() {
        }

        /**
         * Transform flat sections + fields into a nested template tree.
         *
         * @param sections section definitions from TemplateVersion
         * @param fields   field definitions from TemplateVersion
         * @return root TemplateTreeNode with nested children
         */
        public static TemplateTreeNode transform(List<SectionTemplateElementDto> sections,
                        List<FieldTemplateElementDto> fields) {
                // Build the FieldResolver for rule transformation
                FieldResolver fieldResolver = FieldResolver.build(sections, fields);

                // Sort inputs by order
                List<SectionTemplateElementDto> sortedSections = sections == null ? List.of()
                                : sections.stream()
                                                .sorted(Comparator.comparingInt(
                                                                s -> s.getOrder() != null ? s.getOrder() : 0))
                                                .toList();

                List<FieldTemplateElementDto> sortedFields = fields == null ? List.of()
                                : fields.stream()
                                                .sorted(Comparator.comparingInt(
                                                                f -> f.getOrder() != null ? f.getOrder() : 0))
                                                .toList();

                // Pass 1: Collect all children into mutable lists
                // sectionName → mutable list of child nodes (fields + nested sections)
                Map<String, List<TemplateTreeNode>> childrenCollector = new LinkedHashMap<>();
                // sectionName → section config (for parent lookup and building)
                Map<String, SectionTemplateElementDto> sectionConfigMap = new LinkedHashMap<>();
                // sectionName → transformed rules
                Map<String, List<V2Rule>> sectionRulesMap = new LinkedHashMap<>();

                for (SectionTemplateElementDto section : sortedSections) {
                        String sectionName = section.getName();
                        childrenCollector.put(sectionName, new ArrayList<>());
                        sectionConfigMap.put(sectionName, section);
                        sectionRulesMap.put(sectionName,
                                        RuleTransformer.transformRules(section.getRules(), sectionName, sectionName,
                                                        fieldResolver));
                }

                // Attach fields to their parent sections
                List<TemplateTreeNode> orphanFields = new ArrayList<>();
                for (FieldTemplateElementDto field : sortedFields) {
                        String parentName = field.getParent();

                        List<V2Rule> fieldRules = RuleTransformer.transformRules(
                                        field.getRules(), field.getId(), parentName, fieldResolver);

                        TemplateTreeNode fieldNode = TemplateTreeNode.builder()
                                        .nodeId(field.getId())
                                        .type(field.getType() != null ? field.getType().name() : "Text")
                                        .binding(field.getName())
                                        .label(field.getLabel())
                                        .mandatory(Boolean.TRUE.equals(field.getMandatory()) ? true : null)
                                        .optionSet(field.getOptionSet())
                                        .description(field.getDescription())
                                        .validation(mapValidation(field.getValidationRule()))
                                        .rules(fieldRules.isEmpty() ? null : fieldRules)
                                        .children(null) // leaf node
                                        .build();

                        List<TemplateTreeNode> parentChildren = childrenCollector.get(parentName);
                        if (parentChildren != null) {
                                parentChildren.add(fieldNode);
                        } else {
                                log.warn("Field '{}' (id={}) references unknown parent section '{}'. "
                                                + "Attaching to root.", field.getName(), field.getId(), parentName);
                                orphanFields.add(fieldNode);
                        }
                }

                // Pass 2: Build section nodes bottom-up (recursive)
                // Determine which sections are root-level vs nested
                Set<String> builtSections = new HashSet<>();
                List<TemplateTreeNode> rootChildren = new ArrayList<>(orphanFields);

                for (SectionTemplateElementDto section : sortedSections) {
                        String sectionName = section.getName();
                        if (!builtSections.contains(sectionName)) {
                                TemplateTreeNode sectionNode = buildSectionNode(
                                                sectionName, sectionConfigMap, childrenCollector, sectionRulesMap,
                                                sortedSections, builtSections);

                                String sectionParent = section.getParent();
                                if (sectionParent != null && childrenCollector.containsKey(sectionParent)) {
                                        // Nested — add to parent's children (will be included when parent is built)
                                        childrenCollector.get(sectionParent).add(sectionNode);
                                } else {
                                        rootChildren.add(sectionNode);
                                }
                                builtSections.add(sectionName);
                        }
                }

                // Build root
                return TemplateTreeNode.builder()
                                .nodeId("root")
                                .type("root")
                                .children(List.copyOf(rootChildren))
                                .build();
        }

        /**
         * Recursively build a section node, ensuring children (including nested
         * sections)
         * are built first.
         */
        private static TemplateTreeNode buildSectionNode(
                        String sectionName,
                        Map<String, SectionTemplateElementDto> sectionConfigMap,
                        Map<String, List<TemplateTreeNode>> childrenCollector,
                        Map<String, List<V2Rule>> sectionRulesMap,
                        List<SectionTemplateElementDto> allSections,
                        Set<String> builtSections) {

                // First, build any child sections that haven't been built yet
                for (SectionTemplateElementDto childSection : allSections) {
                        String childParent = childSection.getParent();
                        if (sectionName.equals(childParent) && !builtSections.contains(childSection.getName())) {
                                TemplateTreeNode childNode = buildSectionNode(
                                                childSection.getName(), sectionConfigMap, childrenCollector,
                                                sectionRulesMap, allSections, builtSections);
                                childrenCollector.get(sectionName).add(childNode);
                                builtSections.add(childSection.getName());
                        }
                }

                SectionTemplateElementDto config = sectionConfigMap.get(sectionName);
                boolean isRepeater = Boolean.TRUE.equals(config.getRepeatable());
                List<V2Rule> rules = sectionRulesMap.get(sectionName);
                List<TemplateTreeNode> children = childrenCollector.get(sectionName);

                return TemplateTreeNode.builder()
                                .nodeId(sectionName)
                                .type(isRepeater ? "repeater" : "section")
                                .binding(isRepeater ? sectionName : null)
                                .label(config.getLabel())
                                .description(config.getDescription())
                                .rules(rules == null || rules.isEmpty() ? null : rules)
                                .children(children != null ? List.copyOf(children) : List.of())
                                .build();
        }

        /**
         * Map V1 ElementValidationRule to a V2 validation object.
         * Returns null if no validation is defined.
         */
        private static Object mapValidation(ElementValidationRule validationRule) {
                if (validationRule == null) {
                        return null;
                }
                if (validationRule.getExpression() == null && validationRule.getValidationMessage() == null) {
                        return null;
                }

                Map<String, Object> validation = new LinkedHashMap<>();
                if (validationRule.getExpression() != null) {
                        validation.put("expression", validationRule.getExpression());
                }
                if (validationRule.getValidationMessage() != null) {
                        validation.put("message", validationRule.getValidationMessage());
                }
                return validation;
        }
}

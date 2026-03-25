package org.nmcpye.datarun.service.acl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.template.datatemplateelement.DataFieldRule;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.ElementValidationRule;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.RuleAction;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.web.rest.v2.dto.TemplateTreeNode;
import org.nmcpye.datarun.web.rest.v2.formtemplate.service.TemplateTreeTransformer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TemplateTreeTransformer}.
 */
class TemplateTreeTransformerTest {

    private List<SectionTemplateElementDto> sections;
    private List<FieldTemplateElementDto> fields;

    @BeforeEach
    void setUp() {
        // Build sections matching the V1 sample
        SectionTemplateElementDto main = buildSection("main", 100, false);
        SectionTemplateElementDto patients = buildSection("patients", 200, false);
        SectionTemplateElementDto medicines = buildSection("medicines", 300, true);
        SectionTemplateElementDto referrals = buildSection("referrals", 400, false);

        sections = List.of(main, patients, medicines, referrals);

        // Build fields matching the V1 sample
        fields = List.of(
                buildField("visitdate", "LyIGccZ5mna", "main", ValueType.Date, 104, true, null),
                buildField("gender", "eCw9HcbcnW7", "patients", ValueType.SelectOne, 204, true, "Bu2LhXFDicp"),
                buildFieldWithRule("ispregnant", "FjfKSsEWAYf", "patients", ValueType.YesNo, 205, true,
                        "#{gender} == 'FEMALE'", RuleAction.Show),
                buildField("amd", "pHjMRAL4glF", "medicines", ValueType.SelectOne, 301, true, "sYiS5D2qeG8"),
                buildField("druguom", "gjkfAEM4bwc", "medicines", ValueType.SelectOne, 303, true, "k4ZN0nvhrxq"),
                buildField("cm_measures", "W5QDhN3qanw", "referrals", ValueType.SelectOne, 301, true, "vcYOprXCzar"));
    }

    @Test
    @DisplayName("T1: Full sample → correct nesting, ordering, bindings")
    void fullSampleTransform() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(sections, fields);

        assertEquals("root", root.getNodeId());
        assertEquals("root", root.getType());
        assertEquals(4, root.getChildren().size());

        // Check section order
        assertEquals("main", root.getChildren().get(0).getNodeId());
        assertEquals("patients", root.getChildren().get(1).getNodeId());
        assertEquals("medicines", root.getChildren().get(2).getNodeId());
        assertEquals("referrals", root.getChildren().get(3).getNodeId());

        // Check medicines is "repeater" type
        TemplateTreeNode medicinesNode = root.getChildren().get(2);
        assertEquals("repeater", medicinesNode.getType());
        assertEquals("medicines", medicinesNode.getBinding());
        assertEquals(2, medicinesNode.getChildren().size());

        // Check patients is "section" type
        TemplateTreeNode patientsNode = root.getChildren().get(1);
        assertEquals("section", patientsNode.getType());
        assertNull(patientsNode.getBinding(), "Non-repeater sections have null binding");

        // Check fields in medicines
        assertEquals("pHjMRAL4glF", medicinesNode.getChildren().get(0).getNodeId());
        assertEquals("amd", medicinesNode.getChildren().get(0).getBinding());
        assertEquals("SelectOne", medicinesNode.getChildren().get(0).getType());
        assertEquals("sYiS5D2qeG8", medicinesNode.getChildren().get(0).getOptionSet());

        // Check main has 1 field
        assertEquals(1, root.getChildren().get(0).getChildren().size());
        assertEquals("visitdate", root.getChildren().get(0).getChildren().get(0).getBinding());
    }

    @Test
    @DisplayName("T2: Empty template → root with empty children")
    void emptyTemplate() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(List.of(), List.of());

        assertEquals("root", root.getNodeId());
        assertNotNull(root.getChildren());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    @DisplayName("T3: Repeater type vs section type")
    void typeAssignment() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(sections, fields);

        TemplateTreeNode main = root.getChildren().get(0);
        TemplateTreeNode medicines = root.getChildren().get(2);

        assertEquals("section", main.getType());
        assertEquals("repeater", medicines.getType());
    }

    @Test
    @DisplayName("T4: Field ordering preserved within parent")
    void fieldOrdering() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(sections, fields);

        TemplateTreeNode medicines = root.getChildren().get(2);
        // amd (order:301) should come before druguom (order:303)
        assertEquals("amd", medicines.getChildren().get(0).getBinding());
        assertEquals("druguom", medicines.getChildren().get(1).getBinding());
    }

    @Test
    @DisplayName("T5: Rules transformed — not V1 format in tree")
    void rulesTransformed() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(sections, fields);

        TemplateTreeNode patientsNode = root.getChildren().get(1);

        // Find ispregnant field — it has a rule
        TemplateTreeNode ispregnantField = patientsNode.getChildren().stream()
                .filter(n -> "FjfKSsEWAYf".equals(n.getNodeId()))
                .findFirst()
                .orElseThrow();

        assertNotNull(ispregnantField.getRules());
        assertEquals(1, ispregnantField.getRules().size());

        // Verify the rule is V2 format (JsonLogic AST), not V1 string
        var v2Rule = ispregnantField.getRules().get(0);
        assertEquals("rule_FjfKSsEWAYf_0", v2Rule.getRuleId());
        assertEquals("patients", v2Rule.getScope());
        assertNotNull(v2Rule.getCondition());
        assertInstanceOf(Map.class, v2Rule.getCondition());

        // The condition should reference values.gender, not #{gender}
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) v2Rule.getCondition();
        assertTrue(condition.containsKey("=="),
                "Condition should be a JsonLogic == operator, not a V1 string");
    }

    @Test
    @DisplayName("T6: NULL sections/fields handled gracefully")
    void nullInputs() {
        TemplateTreeNode root = TemplateTreeTransformer.transform(null, null);

        assertEquals("root", root.getNodeId());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    @DisplayName("T7: Nested section (repeater inside section) produces correct hierarchy")
    void nestedSectionTransform() {
        // parent section → child repeater inside it
        SectionTemplateElementDto parent = buildSection("parent_section", 100, false);
        SectionTemplateElementDto childRepeater = buildSection("child_repeater", 200, true);
        childRepeater.setParent("parent_section");

        FieldTemplateElementDto parentField = buildField("field_a", "FA001", "parent_section",
                ValueType.Text, 101, false, null);
        FieldTemplateElementDto childField = buildField("item_name", "CF001", "child_repeater",
                ValueType.Text, 201, true, null);

        TemplateTreeNode root = TemplateTreeTransformer.transform(
                List.of(parent, childRepeater),
                List.of(parentField, childField));

        // Root should have 1 child (parent_section), NOT 2
        assertEquals(1, root.getChildren().size(), "Child repeater should be nested, not at root");

        TemplateTreeNode parentNode = root.getChildren().get(0);
        assertEquals("parent_section", parentNode.getNodeId());
        assertEquals("section", parentNode.getType());

        // parent should have 2 children: field_a + child_repeater
        assertEquals(2, parentNode.getChildren().size());

        // Find the nested repeater
        TemplateTreeNode nestedRepeater = parentNode.getChildren().stream()
                .filter(n -> "child_repeater".equals(n.getNodeId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("child_repeater not found in parent"));
        assertEquals("repeater", nestedRepeater.getType());
        assertEquals("child_repeater", nestedRepeater.getBinding());

        // Nested repeater should have 1 child field
        assertEquals(1, nestedRepeater.getChildren().size());
        assertEquals("item_name", nestedRepeater.getChildren().get(0).getBinding());
    }

    @Test
    @DisplayName("T8: Validation rule carried through to V2 tree node")
    void validationCarriedThrough() {
        SectionTemplateElementDto section = buildSection("main", 100, false);

        FieldTemplateElementDto field = buildField("age", "AGE001", "main",
                ValueType.Integer, 101, true, null);
        // Set a validation rule
        ElementValidationRule validationRule = new ElementValidationRule();
        validationRule.setExpression(". > 0 && . < 150");
        validationRule.setValidationMessage(Map.of("en", "Age must be between 1 and 149"));
        field.setValidationRule(validationRule);

        TemplateTreeNode root = TemplateTreeTransformer.transform(
                List.of(section), List.of(field));

        TemplateTreeNode fieldNode = root.getChildren().get(0).getChildren().get(0);
        assertNotNull(fieldNode.getValidation(), "Validation should be carried through");
        assertInstanceOf(Map.class, fieldNode.getValidation());

        @SuppressWarnings("unchecked")
        Map<String, Object> validation = (Map<String, Object>) fieldNode.getValidation();
        assertEquals(". > 0 && . < 150", validation.get("expression"));
        assertNotNull(validation.get("message"));
    }

    private SectionTemplateElementDto buildSection(String name, int order, boolean repeatable) {
        SectionTemplateElementDto section = new SectionTemplateElementDto();
        section.setName(name);
        section.setOrder(order);
        section.setRepeatable(repeatable);
        section.setLabel(Map.of("en", name));
        return section;
    }

    private FieldTemplateElementDto buildField(String name, String id, String parent,
                                               ValueType type, int order,
                                               boolean mandatory, String optionSet) {
        FieldTemplateElementDto field = new FieldTemplateElementDto();
        field.setName(name);
        field.setId(id);
        field.setParent(parent);
        field.setType(type);
        field.setOrder(order);
        field.setMandatory(mandatory);
        field.setOptionSet(optionSet);
        field.setLabel(Map.of("en", name));
        return field;
    }

    private FieldTemplateElementDto buildFieldWithRule(String name, String id, String parent,
                                                       ValueType type, int order,
                                                       boolean mandatory,
                                                       String ruleExpr, RuleAction ruleAction) {
        FieldTemplateElementDto field = buildField(name, id, parent, type, order, mandatory, null);
        DataFieldRule rule = new DataFieldRule();
        rule.setExpression(ruleExpr);
        rule.setAction(ruleAction);
        field.setRules(List.of(rule));
        return field;
    }
}

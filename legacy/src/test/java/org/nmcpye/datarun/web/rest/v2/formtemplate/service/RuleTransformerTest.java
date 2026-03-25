package org.nmcpye.datarun.web.rest.v2.formtemplate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.template.datatemplateelement.DataFieldRule;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.RuleAction;
import org.nmcpye.datarun.web.rest.v2.dto.V2Rule;
import org.nmcpye.datarun.web.rest.v2.formtemplate.service.FieldResolver;
import org.nmcpye.datarun.web.rest.v2.formtemplate.service.RuleTransformer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RuleTransformer}.
 */
class RuleTransformerTest {

    private FieldResolver resolver;

    @BeforeEach
    void setUp() {
        SectionTemplateElementDto main = buildSection("main", false);
        SectionTemplateElementDto patients = buildSection("patients", false);
        SectionTemplateElementDto medicines = buildSection("medicines", true);

        FieldTemplateElementDto visitdate = buildField("visitdate", "LyIGccZ5mna", "main");
        FieldTemplateElementDto gender = buildField("gender", "eCw9HcbcnW7", "patients");
        FieldTemplateElementDto ispregnant = buildField("ispregnant", "FjfKSsEWAYf", "patients");
        FieldTemplateElementDto diagnosed = buildField("diagnosed_disease_type", "UiM4N3o8OiG", "patients");
        FieldTemplateElementDto cm_measures = buildField("cm_measures", "W5QDhN3qanw", "referrals");
        FieldTemplateElementDto amd = buildField("amd", "pHjMRAL4glF", "medicines");

        SectionTemplateElementDto referrals = buildSection("referrals", false);

        resolver = FieldResolver.build(
                List.of(main, patients, medicines, referrals),
                List.of(visitdate, gender, ispregnant, diagnosed, cm_measures, amd));
    }

    @Test
    @DisplayName("R1: Simple equality → JsonLogic == with correct resolved var")
    void simpleEquality() {
        DataFieldRule v1 = new DataFieldRule();
        v1.setExpression("#{gender} == 'FEMALE'");
        v1.setAction(RuleAction.Show);

        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(v1), "FjfKSsEWAYf", "patients", resolver);

        assertEquals(1, result.size());
        V2Rule rule = result.get(0);

        assertEquals("rule_FjfKSsEWAYf_0", rule.getRuleId());
        assertEquals("patients", rule.getScope());
        assertTrue(rule.getTriggers().contains("values.gender"));

        // Condition should be {"==": [{"var": "values.gender"}, "FEMALE"]}
        assertInstanceOf(Map.class, rule.getCondition());
        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) rule.getCondition();
        assertTrue(condition.containsKey("=="));

        @SuppressWarnings("unchecked")
        List<Object> operands = (List<Object>) condition.get("==");
        @SuppressWarnings("unchecked")
        Map<String, Object> varRef = (Map<String, Object>) operands.get(0);
        assertEquals("values.gender", varRef.get("var"));
        assertEquals("FEMALE", operands.get(1));

        // Effect
        assertEquals(1, rule.getEffects().size());
        assertEquals("FjfKSsEWAYf", rule.getEffects().get(0).getTargetNode());
        assertEquals("SHOW", rule.getEffects().get(0).getAction());
    }

    @Test
    @DisplayName("R2: Cross-section reference resolves to values namespace")
    void crossSectionReference() {
        // Rule on a field in "medicines" referencing "gender" from "patients"
        DataFieldRule v1 = new DataFieldRule();
        v1.setExpression("#{gender} == 'FEMALE'");
        v1.setAction(RuleAction.Show);

        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(v1), "pHjMRAL4glF", "medicines", resolver);

        V2Rule rule = result.get(0);
        assertTrue(rule.getTriggers().contains("values.gender"),
                "Cross-section ref should resolve to values.gender, not _row.gender");
    }

    @Test
    @DisplayName("R3: Same-repeater reference resolves to _row namespace")
    void sameRepeaterReference() {
        DataFieldRule v1 = new DataFieldRule();
        v1.setExpression("#{amd} == 'other'");
        v1.setAction(RuleAction.Show);

        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(v1), "pHjMRAL4glF", "medicines", resolver);

        V2Rule rule = result.get(0);
        assertTrue(rule.getTriggers().contains("collections.medicines.amd"),
                "Same-repeater ref should resolve to collections path");

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) rule.getCondition();
        @SuppressWarnings("unchecked")
        List<Object> operands = (List<Object>) condition.get("==");
        @SuppressWarnings("unchecked")
        Map<String, Object> varRef = (Map<String, Object>) operands.get(0);
        assertEquals("_row.amd", varRef.get("var"));
    }

    @Test
    @DisplayName("R4: Multi-clause || produces JsonLogic 'or'")
    void multiClauseOr() {
        DataFieldRule v1 = new DataFieldRule();
        v1.setExpression("#{ispregnant} == 'true'  || #{ispregnant} == true");
        v1.setAction(RuleAction.Show);

        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(v1), "aEjEOwZ9Mk0", "patients", resolver);

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) result.get(0).getCondition();
        assertTrue(condition.containsKey("or"), "Should have 'or' operator");

        @SuppressWarnings("unchecked")
        List<Object> operands = (List<Object>) condition.get("or");
        assertEquals(2, operands.size());
    }

    @Test
    @DisplayName("R5: Unparseable expression uses _legacy escape hatch")
    void unparseableExpression() {
        DataFieldRule v1 = new DataFieldRule();
        v1.setExpression("some.complex.thing()");
        v1.setAction(RuleAction.Show);

        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(v1), "test_node", "patients", resolver);

        @SuppressWarnings("unchecked")
        Map<String, Object> condition = (Map<String, Object>) result.get(0).getCondition();
        assertTrue(condition.containsKey("_legacy"),
                "Unparseable expression should produce _legacy escape hatch");
        assertEquals("some.complex.thing()", condition.get("_legacy"));
        assertTrue(condition.containsKey("_parse_error"));
    }

    @Test
    @DisplayName("R6: Empty rules list returns empty list")
    void emptyRules() {
        List<V2Rule> result = RuleTransformer.transformRules(
                List.of(), "test_node", "patients", resolver);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("R7: Null rules list returns empty list")
    void nullRules() {
        List<V2Rule> result = RuleTransformer.transformRules(
                null, "test_node", "patients", resolver);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("R8: Boolean literal true (not string)")
    void booleanLiteral() {
        Object result = RuleTransformer.parseValue("true");
        assertEquals(true, result);

        Object resultFalse = RuleTransformer.parseValue("false");
        assertEquals(false, resultFalse);
    }

    @Test
    @DisplayName("R9: Action mapping covers all V1 actions")
    void actionMapping() {
        assertEquals("SHOW", RuleTransformer.mapAction(RuleAction.Show));
        assertEquals("HIDE", RuleTransformer.mapAction(RuleAction.Hide));
        assertEquals("ERROR", RuleTransformer.mapAction(RuleAction.Error));
        assertEquals("WARNING", RuleTransformer.mapAction(RuleAction.Warning));
        assertEquals("SET_REQUIRED", RuleTransformer.mapAction(RuleAction.Mandatory));
        assertEquals("ASSIGN", RuleTransformer.mapAction(RuleAction.Assign));
        assertEquals("FILTER", RuleTransformer.mapAction(RuleAction.Filter));
    }

    private SectionTemplateElementDto buildSection(String name, boolean repeatable) {
        SectionTemplateElementDto section = new SectionTemplateElementDto();
        section.setName(name);
        section.setRepeatable(repeatable);
        return section;
    }

    private FieldTemplateElementDto buildField(String name, String id, String parent) {
        FieldTemplateElementDto field = new FieldTemplateElementDto();
        field.setName(name);
        field.setId(id);
        field.setParent(parent);
        return field;
    }
}

package org.nmcpye.datarun.service.acl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.web.rest.v2.formtemplate.service.FieldResolver;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FieldResolver}.
 */
class FieldResolverTest {

    private FieldResolver resolver;

    @BeforeEach
    void setUp() {
        // Template: main (section) → patients (section) → medicines (repeater)
        SectionTemplateElementDto main = buildSection("main", null, false);
        SectionTemplateElementDto patients = buildSection("patients", null, false);
        SectionTemplateElementDto medicines = buildSection("medicines", null, true);

        FieldTemplateElementDto visitdate = buildField("visitdate", "LyIGccZ5mna", "main");
        FieldTemplateElementDto gender = buildField("gender", "eCw9HcbcnW7", "patients");
        FieldTemplateElementDto amd = buildField("amd", "pHjMRAL4glF", "medicines");
        FieldTemplateElementDto druguom = buildField("druguom", "gjkfAEM4bwc", "medicines");

        resolver = FieldResolver.build(
                List.of(main, patients, medicines),
                List.of(visitdate, gender, amd, druguom));
    }

    @Test
    @DisplayName("F1: Field in same section resolves to self scope")
    void sameSection() {
        Optional<FieldResolver.ResolvedField> result = resolver.resolve("amd", "medicines");

        assertTrue(result.isPresent());
        assertEquals("medicines", result.get().ownerSection());
        assertTrue(result.get().ownerIsRepeater());
        assertEquals("_row.amd", result.get().toV2Var());
        assertEquals("collections.medicines.amd", result.get().toTriggerPath());
    }

    @Test
    @DisplayName("F2: Field in non-repeater section resolves to values namespace")
    void nonRepeaterSection() {
        Optional<FieldResolver.ResolvedField> result = resolver.resolve("gender", "patients");

        assertTrue(result.isPresent());
        assertEquals("patients", result.get().ownerSection());
        assertFalse(result.get().ownerIsRepeater());
        assertEquals("values.gender", result.get().toV2Var());
        assertEquals("values.gender", result.get().toTriggerPath());
    }

    @Test
    @DisplayName("F3: Field in adjacent section found after hierarchy exhausted")
    void adjacentSection() {
        // "gender" is in "patients", but we're asking from "medicines" context
        // With root-level sections (no parent chain), this is an adjacent resolution
        Optional<FieldResolver.ResolvedField> result = resolver.resolve("gender", "medicines");

        assertTrue(result.isPresent());
        assertEquals("patients", result.get().ownerSection());
        assertFalse(result.get().ownerIsRepeater());
        assertEquals("values.gender", result.get().toV2Var());
    }

    @Test
    @DisplayName("F4: Unknown field returns empty with warning")
    void unknownField() {
        Optional<FieldResolver.ResolvedField> result = resolver.resolve("nonexistent", "medicines");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("F5: Repeater field → _row namespace; non-repeater → values")
    void namespaceMapping() {
        // Repeater field
        FieldResolver.ResolvedField repeaterField = resolver.resolve("amd", "medicines").orElseThrow();
        assertEquals("_row.amd", repeaterField.toV2Var());

        // Non-repeater field
        FieldResolver.ResolvedField valuesField = resolver.resolve("visitdate", "main").orElseThrow();
        assertEquals("values.visitdate", valuesField.toV2Var());
    }

    @Test
    @DisplayName("F6: isRepeater helper works correctly")
    void isRepeaterHelper() {
        assertTrue(resolver.isRepeater("medicines"));
        assertFalse(resolver.isRepeater("main"));
        assertFalse(resolver.isRepeater("patients"));
        assertFalse(resolver.isRepeater("nonexistent"));
    }

    private SectionTemplateElementDto buildSection(String name, String parent, boolean repeatable) {
        SectionTemplateElementDto section = new SectionTemplateElementDto();
        section.setName(name);
        section.setParent(parent);
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

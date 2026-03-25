package org.nmcpye.datarun.web.rest.v2.datasubmission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure function that translates a V2 canonical {@code formData} blob back into
 * the V1 shape.
 * <p>
 * Canonical (V2) shape:
 *
 * <pre>{@code
 * {
 *   "values":      { "visitdate": "2025-09-27", "gender": "MALE" },
 *   "collections": { "medicines": { "ulid1": { "_index": 1, "amd": "x" } } }
 * }
 * }</pre>
 * <p>
 * V1 shape:
 *
 * <pre>{@code
 * {
 *   "main":      { "visitdate": "2025-09-27" },
 *   "patients":  { "gender": "MALE" },
 *   "medicines": [ { "_id": "ulid1", "_index": 1, "amd": "x", "_parentId": "sub_uid", "_submissionUid": "sub_uid" } ]
 * }
 * }</pre>
 * <p>
 * This class has no Spring dependencies, no state, and no side effects.
 *
 * @author Hamza Assada
 */
public final class SubmissionDenormalizer {

    private static final Logger log = LoggerFactory.getLogger(SubmissionDenormalizer.class);

    private static final String V1_ID_FIELD = "_id";
    private static final String V1_PARENT_ID = "_parentId";
    private static final String V1_SUBMISSION_UID = "_submissionUid";
    private static final String V1_INDEX_FIELD = "_index";

    private static final String V2_PARENT_ID = "_parent_id";
    private static final String V2_INDEX_FIELD = "_index";

    private SubmissionDenormalizer() {
        // Utility class — no instantiation
    }

    /**
     * Denormalize a canonical V2 formData blob back into V1 section-wrapped shape.
     *
     * @param canonical     canonical JsonNode with "values" and "collections"
     *                      top-level keys
     * @param sections      section definitions from TemplateVersion
     * @param fields        field definitions from TemplateVersion (needed to know
     *                      which section each field belongs to)
     * @param submissionUid the submission's UID (needed for _parentId and
     *                      _submissionUid restoration)
     * @param mapper        ObjectMapper for node creation
     * @return V1-shaped formData JsonNode
     */
    public static JsonNode denormalize(JsonNode canonical,
            List<SectionTemplateElementDto> sections,
            List<FieldTemplateElementDto> fields,
            String submissionUid,
            ObjectMapper mapper) {
        ObjectNode result = mapper.createObjectNode();

        if (canonical == null || canonical.isNull() || !canonical.isObject()) {
            return result;
        }

        JsonNode valuesNode = canonical.get("values");
        JsonNode collectionsNode = canonical.get("collections");

        // Build field-to-section mapping: fieldName → sectionName
        Map<String, String> fieldToSection = buildFieldToSectionMap(fields);

        // Build section order for consistent output
        List<SectionTemplateElementDto> orderedSections = sections == null ? Collections.emptyList()
                : sections.stream()
                        .sorted(Comparator.comparingInt(s -> s.getOrder() != null ? s.getOrder() : 0))
                        .toList();

        for (SectionTemplateElementDto section : orderedSections) {
            String sectionName = section.getName();

            if (Boolean.TRUE.equals(section.getRepeatable())) {
                // Denormalize collection → V1 array
                ArrayNode v1Array = denormalizeCollection(
                        sectionName, collectionsNode, submissionUid, mapper);
                result.set(sectionName, v1Array);
            } else {
                // Rebuild section wrapper from flat values
                ObjectNode sectionObj = rebuildSectionFromValues(
                        sectionName, valuesNode, fieldToSection, mapper);
                result.set(sectionName, sectionObj);
            }
        }

        return result;
    }

    /**
     * Denormalize a single collection from V2 identity-keyed map back to V1 array.
     */
    private static ArrayNode denormalizeCollection(String collectionName,
            JsonNode collectionsNode,
            String submissionUid,
            ObjectMapper mapper) {
        ArrayNode array = mapper.createArrayNode();

        if (collectionsNode == null || collectionsNode.isNull()
                || !collectionsNode.has(collectionName)) {
            return array;
        }

        JsonNode collectionMap = collectionsNode.get(collectionName);
        if (collectionMap == null || collectionMap.isNull() || !collectionMap.isObject()) {
            return array;
        }

        // Collect rows and sort by _index for consistent ordering
        List<Map.Entry<String, JsonNode>> rows = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> it = collectionMap.fields();
        while (it.hasNext()) {
            rows.add(it.next());
        }

        // Sort by _index if present, otherwise maintain insertion order
        rows.sort(Comparator.comparingInt(e -> {
            JsonNode indexNode = e.getValue().get(V2_INDEX_FIELD);
            return indexNode != null && indexNode.isNumber() ? indexNode.intValue() : Integer.MAX_VALUE;
        }));

        for (Map.Entry<String, JsonNode> row : rows) {
            String rowId = row.getKey();
            JsonNode rowData = row.getValue();

            if (rowData == null || !rowData.isObject()) {
                continue;
            }

            ObjectNode v1Item = mapper.createObjectNode();

            // Restore _id as a field
            v1Item.put(V1_ID_FIELD, rowId);

            // Copy all data fields, handling V2→V1 metadata conversion
            Iterator<Map.Entry<String, JsonNode>> rowFields = rowData.fields();
            while (rowFields.hasNext()) {
                Map.Entry<String, JsonNode> field = rowFields.next();
                String fieldName = field.getKey();

                switch (fieldName) {
                    case V2_PARENT_ID:
                        // V2 _parent_id → V1 _parentId
                        v1Item.set(V1_PARENT_ID, field.getValue());
                        break;

                    case V2_INDEX_FIELD:
                        // _index stays as-is
                        v1Item.set(V1_INDEX_FIELD, field.getValue());
                        break;

                    default:
                        v1Item.set(fieldName, field.getValue());
                        break;
                }
            }

            // Ensure _parentId is present — if not set by V2, default to submissionUid
            if (!v1Item.has(V1_PARENT_ID)) {
                v1Item.put(V1_PARENT_ID, submissionUid);
            }

            // Ensure _submissionUid is present
            if (!v1Item.has(V1_SUBMISSION_UID)) {
                v1Item.put(V1_SUBMISSION_UID, submissionUid);
            }

            array.add(v1Item);
        }

        return array;
    }

    /**
     * Rebuild a V1 section wrapper by extracting the relevant fields from the flat
     * values map.
     */
    private static ObjectNode rebuildSectionFromValues(String sectionName,
            JsonNode valuesNode,
            Map<String, String> fieldToSection,
            ObjectMapper mapper) {
        ObjectNode sectionObj = mapper.createObjectNode();

        if (valuesNode == null || valuesNode.isNull() || !valuesNode.isObject()) {
            return sectionObj;
        }

        // Find all fields that belong to this section
        for (Map.Entry<String, String> entry : fieldToSection.entrySet()) {
            String fieldName = entry.getKey();
            String owningSection = entry.getValue();

            if (sectionName.equals(owningSection) && valuesNode.has(fieldName)) {
                sectionObj.set(fieldName, valuesNode.get(fieldName));
            }
        }

        return sectionObj;
    }

    /**
     * Build a mapping from field name → section name.
     * This uses the field's {@code parent} property which references the section
     * name.
     */
    private static Map<String, String> buildFieldToSectionMap(List<FieldTemplateElementDto> fields) {
        if (fields == null) {
            return Collections.emptyMap();
        }

        return fields.stream()
                .filter(f -> f.getName() != null && f.getParent() != null)
                .collect(Collectors.toMap(
                        FieldTemplateElementDto::getName,
                        FieldTemplateElementDto::getParent,
                        (existing, replacement) -> {
                            log.warn("Duplicate field name '{}' across sections — using first occurrence", existing);
                            return existing;
                        }));
    }
}

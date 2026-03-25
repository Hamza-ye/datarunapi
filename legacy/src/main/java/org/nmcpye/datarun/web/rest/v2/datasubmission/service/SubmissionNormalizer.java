package org.nmcpye.datarun.web.rest.v2.datasubmission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Pure function that translates a V1 {@code formData} blob into the V2
 * canonical shape.
 * <p>
 * V1 shape: section-wrapped JSON with arrays for repeaters.
 *
 * <pre>{@code
 * {
 *   "main":      { "visitdate": "2025-09-27", "gender": "MALE" },
 *   "medicines": [ { "_id": "ulid1", "amd": "x", "_parentId": "sub_uid", "_submissionUid": "sub_uid" } ]
 * }
 * }</pre>
 * <p>
 * Canonical (V2) shape: flat {@code values} + identity-keyed
 * {@code collections}.
 *
 * <pre>{@code
 * {
 *   "values":      { "visitdate": "2025-09-27", "gender": "MALE" },
 *   "collections": { "medicines": { "ulid1": { "amd": "x" } } }
 * }
 * }</pre>
 * <p>
 * This class has no Spring dependencies, no state, and no side effects.
 * It is purely a data transformer.
 *
 * @author Hamza Assada
 */
public final class SubmissionNormalizer {

    private static final Logger log = LoggerFactory.getLogger(SubmissionNormalizer.class);

    private static final String V1_ID_FIELD = "_id";
    private static final String V1_UID_FIELD = "_uid";
    private static final String V1_PARENT_ID = "_parentId";
    private static final String V1_SUBMISSION_UID = "_submissionUid";
    private static final String V1_INDEX_FIELD = "_index";

    private static final String V2_PARENT_ID = "_parent_id";
    private static final String V2_INDEX_FIELD = "_index";

    private SubmissionNormalizer() {
        // Utility class — no instantiation
    }

    /**
     * Normalize a V1 formData blob into canonical V2 shape.
     *
     * @param v1FormData V1 formData JsonNode (section-wrapped)
     * @param sections   section definitions from TemplateVersion (needed to
     *                   identify repeaters)
     * @param mapper     ObjectMapper for node creation
     * @return canonical JsonNode with "values" and "collections" top-level keys
     */
    public static JsonNode normalize(JsonNode v1FormData,
            List<SectionTemplateElementDto> sections,
            ObjectMapper mapper) {
        ObjectNode root = mapper.createObjectNode();
        ObjectNode values = mapper.createObjectNode();
        ObjectNode collections = mapper.createObjectNode();

        root.set("values", values);
        root.set("collections", collections);

        if (v1FormData == null || v1FormData.isNull() || !v1FormData.isObject()) {
            return root;
        }

        // Build set of repeatable section names
        Set<String> repeaterNames = sections == null ? Collections.emptySet()
                : sections.stream()
                        .filter(s -> Boolean.TRUE.equals(s.getRepeatable()))
                        .map(SectionTemplateElementDto::getName)
                        .collect(Collectors.toSet());

        Iterator<Map.Entry<String, JsonNode>> fields = v1FormData.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String sectionName = entry.getKey();
            JsonNode sectionValue = entry.getValue();

            if (repeaterNames.contains(sectionName)) {
                // Repeater section → V1 stores as JSON array
                normalizeRepeater(sectionName, sectionValue, collections, mapper);
            } else if (sectionValue != null && sectionValue.isObject()) {
                // Non-repeater section → V1 stores as JSON object with field values
                flattenSectionIntoValues(sectionName, sectionValue, values);
            } else {
                // Unexpected structure → preserve as-is in values (defensive)
                log.warn("Unexpected V1 formData structure: key '{}' is neither a repeater array "
                        + "nor a section object. Preserving as-is in values.", sectionName);
                values.set(sectionName, sectionValue);
            }
        }

        return root;
    }

    /**
     * Normalize a V1 repeater array into a V2 identity-keyed collection map.
     */
    private static void normalizeRepeater(String collectionName,
            JsonNode v1Array,
            ObjectNode collections,
            ObjectMapper mapper) {
        ObjectNode collectionMap = mapper.createObjectNode();

        if (v1Array == null || v1Array.isNull() || !v1Array.isArray()) {
            // Empty or missing repeater → empty collection
            collections.set(collectionName, collectionMap);
            return;
        }

        for (JsonNode item : v1Array) {
            if (item == null || !item.isObject()) {
                log.warn("Skipping malformed repeater row in collection '{}': not an object", collectionName);
                continue;
            }

            // Extract the row ID — required (MigrationRepeatIdGenerator guarantees it
            // exists)
            String rowId = extractRowId(item);
            if (rowId == null || rowId.isBlank()) {
                log.error("Repeater row in collection '{}' has no _id — skipping. "
                        + "This should not happen if MigrationRepeatIdGenerator ran first.", collectionName);
                continue;
            }

            // Build the clean row: copy all fields except V1 metadata
            ObjectNode row = mapper.createObjectNode();
            Iterator<Map.Entry<String, JsonNode>> rowFields = item.fields();
            while (rowFields.hasNext()) {
                Map.Entry<String, JsonNode> field = rowFields.next();
                String fieldName = field.getKey();

                switch (fieldName) {
                    case V1_ID_FIELD:
                    case V1_UID_FIELD:
                        // Row ID becomes the map key — don't include as a field
                        break;

                    case V1_SUBMISSION_UID:
                        // V1-only metadata — strip entirely
                        break;

                    case V1_PARENT_ID:
                        // Convert to snake_case for nested repeaters.
                        // Top-level repeaters have _parentId == submissionUid — those get stripped
                        // because the parent relationship is implicit (they're at the root of
                        // collections).
                        // Nested repeaters have _parentId == actual parent row id — those get kept.
                        // Heuristic: if the value looks like a submission UID (11 chars), it's likely
                        // a top-level repeat. But the safest approach is to always include it and let
                        // the denormalizer handle the distinction.
                        // Decision: always include as _parent_id. The denormalizer knows the submission
                        // UID
                        // and can restore the correct V1 value.
                        JsonNode parentValue = field.getValue();
                        if (parentValue != null && !parentValue.isNull()) {
                            row.set(V2_PARENT_ID, parentValue);
                        }
                        break;

                    case V1_INDEX_FIELD:
                        // Preserve _index as-is (sort hint)
                        row.set(V2_INDEX_FIELD, field.getValue());
                        break;

                    default:
                        // Regular data field — copy as-is
                        row.set(fieldName, field.getValue());
                        break;
                }
            }

            collectionMap.set(rowId, row);
        }

        collections.set(collectionName, collectionMap);
    }

    /**
     * Flatten a non-repeater section's fields into the top-level values map.
     * V1 stores: {@code { "main": { "visitdate": "2025-09-27" } }}
     * V2 wants: {@code { "values": { "visitdate": "2025-09-27" } }}
     */
    private static void flattenSectionIntoValues(String sectionName,
            JsonNode sectionObject,
            ObjectNode values) {
        Iterator<Map.Entry<String, JsonNode>> fields = sectionObject.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();

            if (values.has(fieldName)) {
                log.warn("Field name collision during normalization: field '{}' from section '{}' "
                        + "collides with an existing value. The later value wins. "
                        + "This is a V1 design flaw — field names should be unique across sections.",
                        fieldName, sectionName);
            }

            values.set(fieldName, field.getValue());
        }
    }

    /**
     * Extract the row ID from a V1 repeater item.
     * Checks {@code _id} first, falls back to {@code _uid}.
     */
    private static String extractRowId(JsonNode item) {
        JsonNode idNode = item.get(V1_ID_FIELD);
        if (idNode != null && !idNode.isNull() && !idNode.asText("").isBlank()) {
            return idNode.asText();
        }

        // Fallback: legacy _uid field
        JsonNode uidNode = item.get(V1_UID_FIELD);
        if (uidNode != null && !uidNode.isNull() && !uidNode.asText("").isBlank()) {
            return uidNode.asText();
        }

        return null;
    }
}

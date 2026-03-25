package org.nmcpye.datarun.datacapture.datasubmission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.validation.DomainValidationException;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

/**
 * FormDataProcessor
 * <p>
 * Responsibility: when asked (migration mode) generate missing repeat _id values,
 * populate _submissionUid, set _parentId for nested repeats (to the actual parent repeat id),
 * and set _index where missing.
 * <p>
 * This class NEVER overwrites existing _id or _parentId values.
 * It returns a NEW JsonNode (deep-copy) so callers can decide to persist mutated JSON.
 */
@Component
@RequiredArgsConstructor
public class FormDataProcessor {
    private final ObjectMapper objectMapper;

    /**
     * Enrich the submission's formData:
     * - If incoming formData is Map or POJO, convert to JsonNode.
     * - Work on a deep copy and return it.
     *
     * @param submission         the submission (for submission UID)
     * @param generateMissingIds when true: generate missing _id for repeat items; when false: throw on missing _id
     * @return a new JsonNode with generated fields (or same structure if nothing needed)
     */
    public JsonNode enrichFormData(DataSubmission submission, boolean generateMissingIds) {
        Object raw = submission.getFormData();
        JsonNode root = convertToJsonNode(raw);

        // operate on a deep copy to avoid accidental in-place mutation of entity state
        JsonNode mutated;
        if (root.isObject()) {
            mutated = ((ObjectNode) root).deepCopy();
            processObjectNode((ObjectNode) mutated, submission.getUid(), null, generateMissingIds);
        } else if (root.isArray()) {
            mutated = ((ArrayNode) root).deepCopy();
            processArrayNode((ArrayNode) mutated, submission.getUid(), null, generateMissingIds);
        } else {
            // primitive -> nothing to do
            mutated = root;
        }

        return mutated;
    }

    private JsonNode convertToJsonNode(Object raw) {
        if (raw == null) {
            return objectMapper.createObjectNode();
        }
        if (raw instanceof JsonNode) {
            return (JsonNode) raw;
        }
        // covers Map<String,Object> and POJOs as before
        return objectMapper.convertValue(raw, JsonNode.class);
    }

    /**
     * Process an object node: iterate its fields and handle arrays/nested objects.
     *
     * @param node               object node to mutate
     * @param submissionUid      submission id to set in items
     * @param currentParentId    immediate parent repeat id (null when top-level)
     * @param generateMissingIds generate missing _id when true; otherwise throw on missing id
     */
    private void processObjectNode(ObjectNode node,
                                   String submissionUid,
                                   String currentParentId,
                                   boolean generateMissingIds) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (value == null || value.isNull()) {
                // nothing to do
                continue;
            }

            if (value.isArray()) {
                ArrayNode array = (ArrayNode) value;
                // treat array-of-objects as repeat instances
                if (!array.isEmpty() && array.get(0).isObject()) {
                    processRepeatArray(array, submissionUid, currentParentId, generateMissingIds);
                    // replace field with mutated array (already mutated in place)
                    node.set(key, array);
                } else {
                    // array of primitives or nested arrays -> process nested arrays recursively
                    processArrayNode(array, submissionUid, currentParentId, generateMissingIds);
                    node.set(key, array);
                }
            } else if (value.isObject()) {
                // nested object (non-repeat) — recurse while preserving currentParentId
                processObjectNode((ObjectNode) value, submissionUid, currentParentId, generateMissingIds);
                node.set(key, value);
            } /*else {
                // primitive -> no processing required
            }*/
        }
    }

    /**
     * Process a top-level or nested array node that is NOT an array-of-objects repeat (primitives or nested arrays).
     */
    @SuppressWarnings("RedundantCast")
    private void processArrayNode(ArrayNode array,
                                  String submissionUid,
                                  String currentParentId,
                                  boolean generateMissingIds) {
        for (int i = 0; i < array.size(); i++) {
            JsonNode item = array.get(i);
            if (item == null || item.isNull()) continue;
            if (item.isObject()) {
                ObjectNode obj = (ObjectNode) item.deepCopy();
                processObjectNode(obj, submissionUid, currentParentId, generateMissingIds);
                array.set(i, obj);
            } else if (item.isArray()) {
                ArrayNode nested = (ArrayNode) item.deepCopy();
                processArrayNode(nested, submissionUid, currentParentId, generateMissingIds);
                array.set(i, nested);
            } /*else {
                // primitive -> no-op
            }*/
        }
    }

    /**
     * Process an array of objects treated as repeated instances.
     * - ensures _id exists (generate if allowed or throw)
     * - sets _submissionUid when missing
     * - sets _parentId only if currentParentId != null and missing
     * - sets _index when missing
     * - recurses into each item passing its own id as parent for nested repeats
     */
    @SuppressWarnings("RedundantCast")
    private void processRepeatArray(ArrayNode array,
                                    String submissionUid,
                                    String currentParentId,
                                    boolean generateMissingIds) {
        for (int i = 0; i < array.size(); i++) {
            JsonNode rawItem = array.get(i);
            if (rawItem == null || !rawItem.isObject()) {
                // defensive: skip non-object items
                continue;
            }

            ObjectNode item = (ObjectNode) rawItem.deepCopy();

            // ensure _id exists (do not overwrite)
            JsonNode idNode = item.get("_id");
            String id = (idNode == null || idNode.isNull() || idNode.asText().isBlank()) ? null : idNode.asText();

            if (id == null) {
                if (generateMissingIds) {
                    id = CodeGenerator.nextUlid();
                    item.put("_id", id);
                } else {
                    throw new DomainValidationException("Missing repeat _id for a repeat instance in submission " + submissionUid);
                }
            }

            // _submissionUid: always set if missing
            if (!item.has("_submissionUid") || item.get("_submissionUid").isNull()) {
                item.put("_submissionUid", submissionUid);
            }

            // _parentId: set to currentParentId only if we have a parent and parent id is missing
            if (currentParentId != null) {
                if (!item.has("_parentId") || item.get("_parentId").isNull()) {
                    item.put("_parentId", currentParentId);
                }
            } // else: top-level repeat -> leave _parentId alone (do not set to submissionUid)

            // _index: set if missing (1-based)
            if (!item.has("_index") || item.get("_index").isNull()) {
                item.put("_index", i + 1);
            }

            // replace mutated item in array
            array.set(i, item);

            // recurse into this item, passing this item's id as the new parent for nested repeats
            processObjectNode(item, submissionUid, id, generateMissingIds);
        }
    }
}

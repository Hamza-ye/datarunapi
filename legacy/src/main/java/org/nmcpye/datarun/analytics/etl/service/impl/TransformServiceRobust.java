package org.nmcpye.datarun.analytics.etl.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.nmcpye.datarun.analytics.etl.dto.TallCanonicalValue;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class TransformServiceRobust {
    private static final int VALUE_TEXT_MAX = 4000;
    private static final int VALUE_JSON_MAX = 10000;

    /**
     * Transform with option to flatten arrays of primitives.
     * If flattenPrimitiveArrays == false (default desired behavior) then arrays of primitives
     * are kept as valueJson (single row) rather than exploded into multiple rows.
     */
    public List<TallCanonicalValue> transform(JsonNode root,
                                              List<CanonicalElement> elements,
                                              boolean flattenPrimitiveArrays) {
        List<TallCanonicalValue> result = new ArrayList<>();
        Instant now = Instant.now();
        for (CanonicalElement element : elements) {
            Set<String> jsonPaths = element.getJsonDataPaths();
            if (jsonPaths == null || jsonPaths.isEmpty()) continue;

            for (String jsonPath : jsonPaths) {
                List<PathHit> hits = resolvePathHits(root, jsonPath, flattenPrimitiveArrays);

                for (PathHit hit : hits) {
                    JsonNode node = hit.node;
                    if (node == null || node.isMissingNode()) continue;

                    // derive parent / repeat instance IDs using nearest-element and parent-element
                    JsonNode nearestElement = hit.nearestElementNode;
                    JsonNode nearestParent = hit.nearestParentElementNode;

                    // repeatInstanceId = nearestElement._id (if exists), fallback to node._id if you prefer
                    String repeatInstanceId = (nearestElement != null && !nearestElement.isMissingNode())
                        ? textOrNull(nearestElement, "_id")
                        : textOrNull(node, "_id"); // fallback

                    // parentInstanceId = nearestParent._id (the join to the nearest level above)
                    String parentInstanceId = (nearestParent != null && !nearestParent.isMissingNode())
                        ? textOrNull(nearestParent, "_id")
                        : null;

                    // repeatIndex: prefer nearestElement._index, then node._index, then hit.arrayIndex
                    Integer repeatIndex = null;
                    if (nearestElement != null && nearestElement.has("_index")
                        && nearestElement.get("_index").canConvertToInt()) {
                        repeatIndex = nearestElement.get("_index").intValue();
                    } else if (node.has("_index")
                        && node.get("_index").canConvertToInt()) {
                        repeatIndex = node.get("_index").intValue();
                    } else {
                        repeatIndex = hit.arrayIndex;
                    }

                    // Extract typed values (single place)
                    String valueText = null;
                    Boolean valueBool = null;
                    BigDecimal valueNumber = null;
                    String valueJson = null;

                    if (node.isNumber()) {
                        valueNumber = node.decimalValue();
                        valueText = node.asText();
                    } else if (node.isBoolean()) {
                        valueBool = node.asBoolean();
                        valueText = node.asText();
                    } else if (node.isTextual()) {
                        valueText = node.asText();
                    } else if (node.isArray() || node.isObject()) {
                        // If node is an array-of-primitives and we chose to keep it as JSON,
                        // node is the array node (resolvePathHits will have produced that).
                        String raw = node.toString();
                        if (raw.length() > VALUE_JSON_MAX) {
                            valueJson = raw.substring(0, VALUE_JSON_MAX) + "...(truncated)";
                            valueText = raw.substring(0, VALUE_TEXT_MAX) + "...(truncated)";
                        } else {
                            valueJson = raw;
                        }
                    } else {
                        valueText = node.asText(null);
                    }

                    if (valueText != null && valueText.length() > VALUE_TEXT_MAX) {
                        valueText = valueText.substring(0, VALUE_TEXT_MAX) + "...(truncated)";
                    }

                    boolean emptyText = (valueText == null || valueText.isEmpty());
                    boolean emptyNumber = (valueNumber == null);
                    boolean emptyJson = (valueJson == null || valueJson.isEmpty());
                    if (emptyText && emptyNumber && emptyJson) continue;

                    TallCanonicalValue row = TallCanonicalValue.builder()
                        .canonicalElementId(element.getId())
                        .elementPath(hit.elementPath)
                        .repeatInstanceId(repeatInstanceId)
                        .parentInstanceId(parentInstanceId)
                        .repeatIndex(repeatIndex)
                        .valueText(valueText)
                        .valueNumber(valueNumber)
                        .valueBool(valueBool)
                        .valueJson(valueJson)
                        .build();

                    result.add(row);
                }
            }
        }
        return result;
    }

    // ---------------------------
    // PathHit + resolver
    // ---------------------------

    private static class PathHit {
        final JsonNode node;                 // resolved node (leaf or array if we kept primitive array)
        final Integer arrayIndex;            // index when node is an element produced from an array; null otherwise
        final String elementPath;            // normalized path like "visits[0].measurements[1].value"
        final JsonNode nearestElementNode;   // the nearest element node at the current array level (elem)
        final JsonNode nearestParentElementNode; // the immediate parent element node (one level above), may be null

        PathHit(JsonNode node, Integer arrayIndex, String elementPath,
                JsonNode nearestElementNode, JsonNode nearestParentElementNode) {
            this.node = node;
            this.arrayIndex = arrayIndex;
            this.elementPath = elementPath;
            this.nearestElementNode = nearestElementNode;
            this.nearestParentElementNode = nearestParentElementNode;
        }
    }

    /**
     * Resolve dotted path into PathHits. Behavior:
     * - When encountering an array:
     * * If array-of-objects -> iterate elements (each element becomes a new PathHit with nearestElementNode=elem
     * and nearestParentElementNode = ph.nearestElementNode).
     * * If array-of-primitives:
     * - if flattenPrimitiveArrays==true => iterate elements (produce primitive hits)
     * - else (default) => produce a single PathHit for the array node itself (so transform keeps array JSON)
     * - elementPath uses segment[index] only for array elements; when we keep array as JSON we do not append [i].
     */
    private List<PathHit> resolvePathHits(JsonNode root, String dottedPath, boolean flattenPrimitiveArrays) {
        List<PathHit> current = new ArrayList<>();
        current.add(new PathHit(root, null, "", null, null)); // starting hit: no nearest element yet

        String[] segments = dottedPath.split("\\.");
        for (String segment : segments) {
            List<PathHit> next = new ArrayList<>();
            for (PathHit ph : current) {
                JsonNode node = ph.node;
                if (node == null || node.isMissingNode() || node.isNull()) continue;

                JsonNode child = node.get(segment);
                if (child == null || child.isMissingNode() || child.isNull()) continue;

                if (child.isArray()) {
                    // examine array content to decide primitive vs object
                    ArrayNode arr = (ArrayNode) child;
                    boolean anyObject = false;
                    boolean anyPrimitive = false;
                    int inspectLimit = Math.min(arr.size(), 10);
                    for (int i = 0; i < inspectLimit; i++) {
                        JsonNode el = arr.get(i);
                        if (el.isObject()) anyObject = true;
                        else if (el.isValueNode()) anyPrimitive = true;
                    }

                    if (!anyObject) {
                        // likely array of primitives (or empty). Honor flattenPrimitiveArrays flag.
                        if (!flattenPrimitiveArrays) {
                            // create a single PathHit for the array node itself (keeps the array as JSON)
                            String newPath = buildPath(ph.elementPath, segment, -1);
                            // Carry forward nearestElementNode and nearestParentElementNode since
                            // array as JSON belongs to the same nearest element context
                            next.add(new PathHit(child, null, newPath, ph.nearestElementNode, ph.nearestParentElementNode));
                            continue;
                        } else {
                            // flattenPrimitiveArrays == true -> fallthrough to element iteration
                        }
                    }

                    // array-of-objects OR user requested flattening primitives -> iterate elements
                    int size = arr.size();
                    for (int i = 0; i < size; i++) {
                        JsonNode elem = arr.get(i);
                        String newPath = buildPath(ph.elementPath, segment, i);
                        // nearestParentElementNode for this element is the previous nearestElementNode (if any)
                        JsonNode newNearestParent = ph.nearestElementNode;
                        // nearestElementNode for this element is the element itself
                        JsonNode newNearestElement = elem;
                        next.add(new PathHit(elem, i, newPath, newNearestElement, newNearestParent));
                    }
                } else {
                    // not an array: normal field traversal
                    String newPath = buildPath(ph.elementPath, segment, -1);
                    // propagate nearestElement / nearestParent unchanged
                    next.add(new PathHit(child, ph.arrayIndex, newPath, ph.nearestElementNode, ph.nearestParentElementNode));
                }
            }
            current = next;
            if (current.isEmpty()) break;
        }
        return current;
    }

    // same buildPath as yours
    private String buildPath(String base, String seg, int arrayIndex) {
        StringBuilder sb = new StringBuilder();
        if (base != null && !base.isEmpty()) {
            sb.append(base);
            sb.append(".");
        }
        sb.append(seg);
        if (arrayIndex >= 0) sb.append("[").append(arrayIndex).append("]");
        return sb.toString();
    }

    private String textOrNull(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) return null;
        JsonNode v = node.get(field);
        if (v == null || v.isMissingNode() || v.isNull()) return null;
        return v.asText();
    }
}

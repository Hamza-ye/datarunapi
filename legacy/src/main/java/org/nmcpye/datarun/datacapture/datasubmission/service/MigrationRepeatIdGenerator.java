package org.nmcpye.datarun.datacapture.datasubmission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.analytics.etl.model.TemplateElementMap;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Migration helper that generates missing repeat item IDs in a submission's formData.
 * Used only in migration mode, after-migration production submissions we no longer generate,
 * we validate missing IDs earlier and reject unless explicitly in migration mode.
 * <p>
 * This variant sets nested _parentId to the actual parent repeat id
 * <p>
 * first convert incoming formData to an ObjectNode (a deep copy if desired) — e.g.:
 *
 * <pre>
 * {@code
 *  ObjectNode rootCopy = original.isObject() ? ((ObjectNode) original).deepCopy() : objectMapper.createObjectNode();
 * 	MigrationRepeatIdGenerator gen = new MigrationRepeatIdGenerator(objectMapper, templateElementMap);
 * 	int generated = gen.generateMissingIdsForMigration(rootCopy, submissionUid);
 * 	if (generated > 0) {
 * 		submission.setFormData(rootCopy); // persist mutated formData in migration
 *  }
 * }
 * </pre>
 *
 * @author Hamza Assada 15/08/2025 (7amza.it@gmail.com)
 */
public final class MigrationRepeatIdGenerator {

    private static final Logger log = LoggerFactory.getLogger(MigrationRepeatIdGenerator.class);
    private static final String UID_FIELD = "_id";         // client-side id field
    private static final String PARENT_FIELD = "_parentId";
    private static final String SUBMISSION_UID_FIELD = "_submissionUid";
    private static final String INDEX_FIELD = "_index";

    private final TemplateElementMap elementMap;

    public MigrationRepeatIdGenerator(TemplateElementMap elementMap) {
        this.elementMap = elementMap;
    }

    /**
     * Walks the provided root (must be an ObjectNode or converted to one).
     * Generates missing repeat IDs for any configured repeatable sections.
     *
     * @param rootCopy      The formData root as a mutable ObjectNode (a deep copy is recommended by caller)
     * @param submissionUid The submission UID to set as fallback _submissionUid / parent
     * @return number of generated IDs (0 if none), and the mutated rootCopy is modified in-place.
     */
    public int generateMissingIdsForMigration(ObjectNode rootCopy, String submissionUid) {
        if (rootCopy == null) return 0;
        // collect existing ids to avoid collisions (within this submission)
        final Set<String> seenIds = new HashSet<>();
        collectExistingIds(rootCopy, seenIds);

        // mutated count
        final int[] generated = {0};

        // traverse tree, starting with no parent repeat id (top-level repeats parent -> submissionUid)
        traverseAndGenerate(rootCopy, null, submissionUid, seenIds, generated);

        if (generated[0] > 0) {
            log.info("Migration generated {} missing repeat IDs for submission {}", generated[0], submissionUid);
        } else {
            log.debug("No missing repeat IDs found for submission {}", submissionUid);
        }
        return generated[0];
    }

    /**
     * Recursive traversal that now carries the currentRepeatId (null at root/top-level).
     *
     * @param node          current JSON node (object)
     * @param currentPath   dotted path to current node
     * @param submissionUid top-level submission id (fallback parent)
     * @param seenIds       set to track generated & existing ids
     * @param generated     counter holder (mutated in place)
     */
    private void traverseAndGenerate(JsonNode node,
                                     String currentPath,
                                     String submissionUid,
                                     Set<String> seenIds,
                                     int[] generated) {

        traverseAndGenerate(node, currentPath, currentRepeatIdOf(node, currentPath), submissionUid, seenIds, generated);
    }

    /**
     * @param node            current JSON node (object)
     * @param currentPath     dotted path to current node
     * @param currentRepeatId the repeat id of the immediate parent repeat instance (or null if top-level)
     * @param submissionUid   top-level submission id (fallback parent)
     * @param seenIds         set to track generated & existing ids
     * @param generated       counter holder (mutated in place)
     */
    // helper overload to carry explicit currentRepeatId
    private void traverseAndGenerate(JsonNode node,
                                     String currentPath,
                                     String currentRepeatId,
                                     String submissionUid,
                                     Set<String> seenIds,
                                     int[] generated) {

        if (node == null || node.isNull()) return;
        if (!node.isObject()) return;

        ObjectNode obj = (ObjectNode) node;
        Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            String childPath = (currentPath == null || currentPath.isEmpty()) ? key : currentPath + "." + key;
            AbstractElement element = elementMap.getElementByIdPathMap().get(childPath);

//            if (element instanceof FormSectionConf section && Boolean.TRUE.equals(section.getRepeatable())) {
                // If the value isn't an array, skip (no repeat instances present)
                if (value == null || !value.isArray()) {
                    continue;
                }
                ArrayNode array = (ArrayNode) value;
                for (int i = 0; i < array.size(); i++) {
                    JsonNode item = array.get(i);
                    if (item == null || !item.isObject()) {
                        // defensive: skip malformed items
                        continue;
                    }

                    ObjectNode itemObj = (ObjectNode) item;

                    // existing id detection (_id or _uid fallback)
                    String existingId = null;
                    if (itemObj.has(UID_FIELD) && !itemObj.get(UID_FIELD).isNull()) {
                        existingId = itemObj.get(UID_FIELD).asText(null);
                    } else if (itemObj.has("_uid") && !itemObj.get("_uid").isNull()) {
                        existingId = itemObj.get("_uid").asText(null);
                    }

                    if (existingId != null && !existingId.isBlank()) {
                        seenIds.add(existingId);
                    } else {
                        // generate unique id
                        String gen;
                        do {
                            gen = CodeGenerator.nextUlid();
                        } while (seenIds.contains(gen));
                        seenIds.add(gen);
                        generated[0]++;

                        itemObj.put(UID_FIELD, gen);
                    }

                    // determine the correct parent id: prefer explicit currentRepeatId (immediate parent repeat instance),
                    // otherwise fall back to submissionUid
                    String parentToSet = currentRepeatId != null ? currentRepeatId : submissionUid;
                    if (!itemObj.has(PARENT_FIELD) || itemObj.get(PARENT_FIELD).isNull()) {
                        itemObj.put(PARENT_FIELD, parentToSet);
                    }

                    // set submission id if missing
                    if (!itemObj.has(SUBMISSION_UID_FIELD) || itemObj.get(SUBMISSION_UID_FIELD).isNull()) {
                        itemObj.put(SUBMISSION_UID_FIELD, submissionUid);
                    }

                    // set index if not present
                    if (!itemObj.has(INDEX_FIELD) || itemObj.get(INDEX_FIELD).isNull()) {
                        itemObj.put(INDEX_FIELD, i + 1);
                    }

                    // replace mutated item back into array
                    array.set(i, itemObj);

                    // compute this item's id to pass as parentId to nested repeats
                    String thisItemId = itemObj.has(UID_FIELD) ? itemObj.get(UID_FIELD).asText(null) : null;

                    // recurse into the item: pass thisItemId as the new currentRepeatId so nested repeats get correct _parentId
                    traverseAndGenerate(itemObj, childPath, thisItemId, submissionUid, seenIds, generated);
                }
//                continue;
//            }

            // nested object -> recurse while preserving currentRepeatId (no change)
            if (value.isObject()) {
                traverseAndGenerate(value, childPath, currentRepeatId, submissionUid, seenIds, generated);
            }

            // arrays that are not repeat sections are handled by normalizer; for migration we don't need special handling here
        }
    }

    // best-effort to discover current repeat id from node if available; used only if caller didn't pass explicit parent
    // (not strictly necessary but provides some safety for initial call path).
    private String currentRepeatIdOf(JsonNode node, String currentPath) {
        // This helper is intentionally simple and returns null.
        // The primary recursion entrypoint passes explicit parent ids during recursion.
        return null;
    }

    // Collect any existing ids in the form payload so we avoid collisions
    private void collectExistingIds(JsonNode node, Set<String> seen) {
        if (node == null || node.isNull()) return;
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                JsonNode val = e.getValue();
                if (val != null && val.isObject()) {
                    JsonNode idNode = val.get(UID_FIELD);
                    if (idNode != null && !idNode.isNull() && !idNode.asText().isBlank()) {
                        seen.add(idNode.asText());
                    }
                    collectExistingIds(val, seen);
                } else if (val != null && val.isArray()) {
                    for (JsonNode item : val) {
                        collectExistingIds(item, seen);
                    }
                }
            }
        } else if (node.isArray()) {
            for (JsonNode item : node) collectExistingIds(item, seen);
        }
    }
}

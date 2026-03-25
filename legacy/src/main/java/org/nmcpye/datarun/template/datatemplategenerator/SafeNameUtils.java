package org.nmcpye.datarun.template.datatemplategenerator;

import lombok.NoArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;

import java.util.*;

/**
 * @author Hamza Assada
 * @since 09/09/2025
 */
@NoArgsConstructor
public final class SafeNameUtils {
    /**
     * Return list of ancestor names (nearest parent first) for a canonical path.
     * Uses PathUtils.getDirectParent(path) to step up. The returned list contains
     * the last path segment (name) of each ancestor path.
     * <p>
     * Example: if canonicalPath = "/a/b/c", returns ["b", "a"] (nearest-first).
     */
    private static List<String> ancestorNamesNearestFirst(String canonicalPath) {
        List<String> res = new ArrayList<>();
        if (canonicalPath == null || canonicalPath.isBlank()) return res;

        String parent = PathUtils.getDirectParent(canonicalPath);
        while (parent != null && !parent.isBlank()) {
            // extract last path segment -> name
            String seg;
            int idx = parent.lastIndexOf('/');
            if (idx >= 0 && idx < parent.length() - 1) seg = parent.substring(idx + 1);
            else seg = parent; // fallback if no slash
            if (!seg.isBlank()) res.add(seg);
            parent = PathUtils.getDirectParent(parent);
        }
        return res;
    }

    /**
     * Ensure unique safe names across template elements by progressively
     * prepending ancestors (nearest parent already used), then appending a deterministic
     * numeric suffix if needed.
     * <p>
     * This modifies TemplateElement.safeName (via setter) and updates canonicalByUid
     * CanonicalElement.setSafeName(...) so canonical objects match.
     */
    public static void ensureUniqueSafeNamesBasePreferred(Map<String, CanonicalElement> canonicalByUid) {

        var elements = canonicalByUid.values();
        // 1) initial safe name: base-only (no parent prefix)
        for (CanonicalElement e : elements) {
            String baseName = e.getPreferredName() != null ? e.getPreferredName() : "unnamed";
            String safe = toSafeColumnName(baseName);
            e.setSafeName(safe);
            CanonicalElement ce = canonicalByUid.get(e.getId());
            if (ce != null) ce.setSafeName(safe);
        }

        // 2) build buckets of equal safe names
        Map<String, List<CanonicalElement>> buckets = new LinkedHashMap<>();
        for (CanonicalElement e : elements) {
            buckets.computeIfAbsent(e.getSafeName(), k -> new ArrayList<>()).add(e);
        }

        // 3) taken names set seeded with currently used names (base-safe names)
        Set<String> taken = new HashSet<>(buckets.keySet());

        // Helper to get ancestor list nearest-first (parent first), same as before.
        // Re-use ancestorNamesNearestFirst(...) from earlier or ensure it's in class.

        // 4) resolve only collisions (groups size > 1)
        for (Map.Entry<String, List<CanonicalElement>> be : buckets.entrySet()) {
            List<CanonicalElement> group = be.getValue();
            if (group.size() <= 1) continue;

            // deterministic ordering for stability
            group.sort(Comparator.comparing(CanonicalElement::getCanonicalPath,
                Comparator.nullsFirst(String::compareTo)));

            // For each element in the collision group, try progressively deeper prefixing
            // using parent, grandparent, etc., until unique (or fallback to numeric suffix).
            for (CanonicalElement cur : group) {
                // If someone else already took the base name (and it's not this element), we must change
                String baseCandidate = cur.getSafeName(); // base-only
                boolean conflictExists = false;
                for (CanonicalElement other : elements) {
                    if (other == cur) continue;
                    if (baseCandidate.equals(other.getSafeName())) {
                        conflictExists = true;
                        break;
                    }
                }
                // also consider taken (seeded from others)
                if (!conflictExists && Collections.frequency(
                    elements.stream().map(CanonicalElement::getSafeName).toList(), baseCandidate) > 1) {
                    conflictExists = true;
                }

                if (!conflictExists && !isTakenByDifferent(baseCandidate, cur, elements)) {
                    // surprisingly unique (rare) — ensure it's reserved
                    taken.add(baseCandidate);
                    continue;
                }

                // Need to resolve: get ancestors nearest-first (parent, grandparent, ...)
                List<String> ancestors = ancestorNamesNearestFirst(cur.getCanonicalPath());

                // We'll attempt progressively longer prefixes:
                // first try parent + "_" + base (if parent exists), then grandparent_parent_base, ...
                boolean resolved = false;
                String candidate = baseCandidate;
                String working = baseCandidate;
                for (String anc : ancestors) {
                    if (anc == null || anc.isBlank()) continue;
                    String ancSafe = toSafeColumnName(anc);
                    working = ancSafe + "_" + working; // prepend nearest-first iteratively
                    if (!taken.contains(working)) {
                        candidate = working;
                        taken.add(candidate);
                        resolved = true;
                        break;
                    }
                }

                if (!resolved) {
                    // Ancestors exhausted or none helped — append deterministic numeric suffix.
                    // Use the index in the sorted group (idx) to keep deterministic behavior,
                    // but ensure the attempt is not taken; increment suffix until free.
                    // working contains last tried prepends or base
                    int suffix = 2; // start with _2 to indicate collision
                    String attempt = working + "_" + suffix;
                    while (taken.contains(attempt)) {
                        suffix++;
                        attempt = working + "_" + suffix;
                    }
                    candidate = attempt;
                    taken.add(candidate);
                }

                // assign
                cur.setSafeName(candidate);
                CanonicalElement ce = canonicalByUid.get(cur.getId());
                if (ce != null) ce.setSafeName(candidate);
            }
        }
    }

    /**
     * Returns true if 'name' is already used by any element other than 'self'.
     * Slightly simpler helper used above to check whether baseCandidate is free.
     */
    private static boolean isTakenByDifferent(String name, CanonicalElement self, Collection<CanonicalElement> all) {
        for (CanonicalElement e : all) {
            if (e == self) continue;
            if (name.equals(e.getSafeName())) return true;
        }
        return false;
    }


    /**
     * Max length for safe column names (common choice for many DBs is 63 for Postgres).
     * Adjust if your DB requires a different max.
     */
    private static final int MAX_SAFE_NAME_LENGTH = 63;

    /**
     * Convert an arbitrary string into a deterministic, column-safe identifier:
     * - normalize unicode to remove diacritics
     * - replace non-alphanumeric characters with underscores
     * - collapse multiple underscores
     * - trim leading/trailing underscores
     * - lowercase
     * - ensure it doesn't start with a digit (prefix with 'c' if it does)
     * - truncate to MAX_SAFE_NAME_LENGTH
     * <p>
     * This function is deterministic and does not hash the input.
     */
    private static String toSafeColumnName(String raw) {
        if (raw == null) raw = "unnamed";

        // 1. normalize and remove diacritics
        String normalized = java.text.Normalizer.normalize(raw, java.text.Normalizer.Form.NFKD)
            .replaceAll("\\p{M}", ""); // strip diacritical marks

        // 2. replace non-alphanumeric with underscore
        String replaced = normalized.replaceAll("[^A-Za-z0-9]", "_");

        // 3. collapse multiple underscores
        String collapsed = replaced.replaceAll("_+", "_");

        // 4. trim leading/trailing underscores
        String trimmed = collapsed.replaceAll("^_+|_+$", "");

        // 5. lowercase
        String lower = trimmed.toLowerCase(Locale.ROOT);

        // 6. ensure not empty
        if (lower.isBlank()) {
            lower = "unnamed";
        }

        // 7. ensure it does not start with a digit (prefix with 'c' deterministically)
        if (Character.isDigit(lower.charAt(0))) {
            lower = "c_" + lower;
        }

        // 8. truncate to max length
        if (lower.length() > MAX_SAFE_NAME_LENGTH) {
            lower = lower.substring(0, MAX_SAFE_NAME_LENGTH);
            // optional: trim trailing underscore after truncation
            lower = lower.replaceAll("_+$", "");
            if (lower.isBlank()) lower = "unnamed";
        }

        return lower;
    }
}


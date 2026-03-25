package org.nmcpye.datarun.template.datatemplategenerator;

import lombok.NoArgsConstructor;

/**
 * @author Hamza Assada
 * @since 09/09/2025
 */
@NoArgsConstructor
public final class PathUtils {
    public static String getDirectParent(String path) {
        if (path == null || !path.contains(".")) {
            return null;
        }

        String[] parts = path.split("\\.");

        // Get the direct parent
        return parts.length > 1 ? parts[parts.length - 2] : null;
    }

    public static String replaceLastElement(String path, String newElement) {
        if (path == null || !path.contains(".")) {
            return newElement;
        }

        String[] parts = path.split("\\.");

        parts[parts.length - 1] = newElement;

        return String.join(".", parts);
    }

    public static String generatePath(String parentPath, String fieldName, Integer repeatIndex) {
        if (repeatIndex != null) {
            return parentPath + "[" + repeatIndex + "]." + fieldName;
        }
        return parentPath + "." + fieldName;
    }

    /**
     * Replace last segment of idPath with fieldName (structural name, not display label).
     * If fieldName is null/blank, returns idPath (caller may fallback later).
     */
    public static String computeDataPath(String idPath, String fieldName) {
        if (idPath == null || idPath.isBlank()) return idPath;
        String[] parts = idPath.split("\\.", -1);
        if (parts.length == 0) return idPath;

        int last = parts.length - 1;
        String safeName = sanitizeSegment(fieldName);
        if (safeName == null || safeName.isEmpty()) {
            return idPath;
        }
        parts[last] = safeName;
        return String.join(".", parts);
    }

    private static String sanitizeSegment(String seg) {
        if (seg == null) return null;
        String s = seg.trim();
        if (s.isEmpty()) return s;
        // Simple sanitization: replace dot to avoid breaking path semantics.
        return s.replace(".", "·");
    }
}


package org.nmcpye.datarun.analytics.util;

import java.util.*;
import java.util.stream.Collectors;

public final class UuidUtils {
    private UuidUtils() {
    }

    public static UUID toUuid(String s) {
        if (s == null) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid UUID string: " + s, ex);
        }
    }

    public static UUID toUuidOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof UUID) return (UUID) o;
        if (o instanceof String) return toUuid((String) o);
        throw new IllegalArgumentException("Unsupported id type: " + o.getClass());
    }

    public static boolean isUuid(String s) {
        if (s == null) return false;
        try {
            UUID.fromString(s);
            return true;
        } catch (Exception ex) {
           return false;
        }
    }

    public static List<UUID> toUuidList(Collection<?> inputs) {
        if (inputs == null) return Collections.emptyList();
        return inputs.stream()
            .map(UuidUtils::toUuidOrNull)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public static String toStringOrNull(UUID u) {
        return u == null ? null : u.toString();
    }
}

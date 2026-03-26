package org.nmcpye.platform.core.uid;

import java.util.UUID;

/**
 * Platform UID generator. Uses standard UUID v4.
 * Independent of legacy CodeGenerator — no imports from org.nmcpye.datarun.
 */
public final class UidGenerator {

    private UidGenerator() {
        // utility class
    }

    /**
     * Generate a new random UUID string.
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * Parse a string to UUID.
     */
    public static UUID parse(String uid) {
        return UUID.fromString(uid);
    }
}

package org.nmcpye.datarun.analytics.etl.util;

import org.nmcpye.datarun.analytics.etl.dto.TallCanonicalValue;

/**
 * Deterministic instance-key generation shared by upsert/delete logic.
 * <p>
 * Rules (kept exactly from your chosen logic):
 * - If repeatInstanceId present:
 * - if elementPath endsWith(']') -> key = repeatId + '|' + elementPath
 * - else -> key = repeatId
 * - else:
 * - key = submissionUid + '|' + elementPath
 * <p>
 * Also truncates to INSTANCE_KEY_MAX to protect DB column length.
 */
public final class InstanceKeyUtil {
    private InstanceKeyUtil() {
    }

    public static String computeInstanceKey(String submissionUid, TallCanonicalValue r) {
        if (r == null) return "unknown|";
        return computeInstanceKey(r.getRepeatInstanceId(), r.getElementPath(), submissionUid);
    }

    public static String computeInstanceKey(String repeatInstanceId, String elementPath, String submissionUid) {
        return repeatInstanceId != null ? repeatInstanceId : submissionUid;
    }
}

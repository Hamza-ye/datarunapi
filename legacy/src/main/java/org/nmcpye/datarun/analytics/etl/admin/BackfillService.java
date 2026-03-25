package org.nmcpye.datarun.analytics.etl.admin;

import java.util.List;

public interface BackfillService {
    /**
     * Enqueue backfill for explicit submission IDs.
     * Returns number of rows actually inserted.
     */
    int enqueueBySubmissionIds(List<String> submissionIds);

    /**
     * Enqueue backfill for a serial-number range (inclusive).
     * Returns number of rows actually inserted.
     */
    int enqueueBySerialRange(Long fromSerial, Long toSerial);
}

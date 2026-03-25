package org.nmcpye.datarun.outbox.service;

import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.nmcpye.datarun.outbox.entity.OutboxProcessing;

import java.time.Instant;
import java.util.UUID;

/**
 * Records per-outbox processing outcomes into outbox_processing and updates outbox rows.
 * <p>
 * Responsibilities:
 * - record processing attempt = 'processing'
 * - record success/failure with error info
 * - optionally implement retries, exponential backoff logic later
 */
public interface OutboxProcessingService {

    /**
     * Record the start of processing for an outbox row within the run.
     * Insert an 'processing' row into outbox_processing.
     */
    OutboxProcessing startProcessing(UUID etlRunId, OutboxDto outbox);

    /**
     * Record success for the outbox row.
     * Implementation should:
     * - insert outbox_processing status 'success'
     * - update outbox.status = 'success'
     * - ensure idempotency (do not double-insert)
     */
    void recordSuccess(UUID etlRunId, OutboxDto outbox);

    /**
     * Record failure for the outbox row and set next attempt time (if any).
     * Implementation should:
     * - insert outbox_processing status 'failure' with error details
     * - update outbox attempt/time fields for retry
     */
    void recordFailure(UUID etlRunId, OutboxDto outbox, String error, Instant nextAttemptAt);
}

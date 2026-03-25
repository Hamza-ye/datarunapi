package org.nmcpye.datarun.outbox.service;

import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;

import java.util.List;
import java.util.UUID;

/**
 * Claiming service for outbox rows. This uses NamedParameterJdbcTemplate based repository
 * to atomically claim rows (assign ingest_id) for a run.
 * <p>
 * Responsibilities:
 * - claim batches using SKIP LOCKED / UPDATE ... RETURNING
 * - return list of OutboxDto for processing
 */
public interface OutboxClaimService {

    /**
     * Claim up to batchSize pending rows and associate them with the given EtlRun.
     * <p>
     * The method will:
     * - generate or accept an ingestId
     * - atomically assign ingestId to selected outbox rows
     * - return claimed OutboxDto rows
     * <p>
     * NOTE: implementers should ensure concurrency-safe semantics (SKIP LOCKED).
     */
    List<OutboxDto> claimBatch(EtlRun run, int batchSize);

    /**
     * Convenience method to claim using an externally supplied ingestId.
     */
    List<OutboxDto> claimBatchWithIngestId(UUID ingestId, String claimedBy, int batchSize);
}

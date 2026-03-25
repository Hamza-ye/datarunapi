package org.nmcpye.datarun.outbox.service.impl;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;
import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.nmcpye.datarun.outbox.repository.OutboxClaimPort;
import org.nmcpye.datarun.outbox.service.OutboxClaimService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service that claims pending outbox rows for processing.
 * <p>
 * Notes:
 * - This delegates to OutboxJdbcRepository.claimPendingBatch which uses FOR UPDATE SKIP LOCKED
 * to safely claim rows in concurrent consumer scenarios.
 * - Keep this layer thin; it may later add metrics, throttling, or claim-scoping logic.
 */
@Service
@RequiredArgsConstructor
public class OutboxClaimServiceImpl implements OutboxClaimService {

    private static final Logger log = LoggerFactory.getLogger(OutboxClaimServiceImpl.class);

    private final OutboxClaimPort outboxJdbcRepository;

    /**
     * Claim a batch for the given run. Generates a new ingestId and delegates to claimBatchWithIngestId.
     * <p>
     * The generated ingestId groups the claimed rows and can be persisted to outbox.ingest_id
     * so downstream processing can use it as an idempotency/provenance token.
     */
    @Override
    public List<OutboxDto> claimBatch(EtlRun run, int batchSize) {
        UUID ingestId = UUID.randomUUID();
        // We intentionally do not persist the ingestId into EtlRun here; caller/ orchestrator manages run metadata.
        return claimBatchWithIngestId(ingestId, run.getCreatedBy(), batchSize);
    }

    /**
     * Claim up to batchSize pending outbox rows and assign them to ingestId.
     * <p>
     * Concurrency:
     * - Under the hood we use "FOR UPDATE SKIP LOCKED" in the JDBC repository. This ensures multiple
     * concurrent claimers do not pick the same rows; SKIP LOCKED causes locked rows to be skipped.
     *
     * @param ingestId  UUID to assign to claimed rows
     * @param batchSize max rows to claim
     * @return list of claimed OutboxDto (may be empty)
     */
    @Override
    public List<OutboxDto> claimBatchWithIngestId(UUID ingestId, String claimedBy, int batchSize) {
        // We intentionally do not persist the ingestId into EtlRun here; caller/ orchestrator manages run metadata.
        log.debug("Attempting to claim up to {} outbox rows with ingestId={}", batchSize, ingestId);
        List<OutboxDto> claimed = outboxJdbcRepository.claimPendingBatch(ingestId, claimedBy, batchSize);
        int n = (claimed == null) ? 0 : claimed.size();
        log.debug("Claimed {} outbox rows with ingestId={}", n, ingestId);
        return (claimed == null) ? List.of() : claimed;
    }
}

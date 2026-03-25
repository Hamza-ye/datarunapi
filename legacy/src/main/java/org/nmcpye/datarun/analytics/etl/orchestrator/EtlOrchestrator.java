package org.nmcpye.datarun.analytics.etl.orchestrator;

import org.nmcpye.datarun.analytics.etl.dto.EtlRunCompletion;
import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;
import org.nmcpye.datarun.analytics.etl.service.EtlRunService;
import org.nmcpye.datarun.analytics.etl.service.EventProcessorService;
import org.nmcpye.datarun.outbox.service.OutboxClaimService;
import org.nmcpye.datarun.outbox.service.OutboxProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

/**
 * Orchestrator that runs one ETL cycle.
 * <p>
 * Important: per-outbox work is executed inside a dedicated transaction so each outbox either
 * fully commits its transform/persist/audit or rolls back; failures persist a failure record
 * via REQUIRES_NEW in EventProcessorServiceImpl.
 */
@Component
public class EtlOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(EtlOrchestrator.class);

    private final EtlRunService etlRunService;
    private final OutboxClaimService outboxClaimService;
    private final OutboxProcessingService outboxProcessingService;
    private final EventProcessorService eventProcessorService;
    private final TransactionTemplate txTemplate;

    public EtlOrchestrator(EtlRunService etlRunService,
                           OutboxClaimService outboxClaimService,
                           OutboxProcessingService outboxProcessingService,
                           EventProcessorService eventProcessorService,
                           PlatformTransactionManager txManager) {
        this.etlRunService = etlRunService;
        this.outboxClaimService = outboxClaimService;
        this.outboxProcessingService = outboxProcessingService;
        this.eventProcessorService = eventProcessorService;

        // default template (not used for per-outbox work anymore)
        this.txTemplate = new TransactionTemplate(txManager);
    }

    /**
     * Run a single processing cycle:
     * - start run -> claim batch -> for each outbox: per-outbox TX { startProcessing; processEvent }
     * - finish run and persist counters
     */
    public void runOnce(String pipelineName, int batchSize) {
        String createdBy = "etl-orchestrator";
        String codeVersion = System.getenv().getOrDefault("CODE_VERSION", "unknown");

        EtlRun run = etlRunService.startRun(pipelineName, createdBy, codeVersion);
        UUID runId = run.getEtlRunId();
        UUID ingestId = UUID.randomUUID();

        long submittedCount = 0;
        long successCount = 0;
        long failureCount = 0;
        long minSerial = -1L;
        long maxSerial = -1L;

        try {
            List<OutboxDto> claimed = outboxClaimService.claimBatchWithIngestId(ingestId, pipelineName, batchSize);
            minSerial = claimed.stream().mapToLong(OutboxDto::getSubmissionSerialNumber).min().orElse(-1L);
            maxSerial = claimed.stream().mapToLong(OutboxDto::getSubmissionSerialNumber).max().orElse(-1L);

            submittedCount = (claimed == null) ? 0 : claimed.size();
            log.info("Run {} claimed {} rows (ingestId={})", runId, submittedCount, ingestId);

            if (claimed != null) {
                for (OutboxDto outbox : claimed) {
                    try {
                        // Per-outbox transaction: startProcessing + processEvent (transform + upsert/delete + recordSuccess)
                        txTemplate.execute(status -> {
                            // insert 'processing' audit row in same TX
                            outboxProcessingService.startProcessing(runId, outbox);

                            // processEvent does transform (pure) + persistence (upsert/delete) and on success records success.
                            // On failure it will call recordFailureRequiresNewTx(...) to persist failure durable, then rethrow.
                            try {
                                eventProcessorService.processEvent(runId, outbox, ingestId);
                            } catch (RuntimeException | Error e) {
                                // rethrow to cause TX rollback for this outbox
                                throw e;
                            } catch (Exception e) {
                                // wrap checked exceptions to ensure rollback
                                throw new RuntimeException(e);
                            }
                            return null;
                        });
                        // if we reach here, the per-outbox tx committed and recordSuccess has been called by EventProcessorService
                        successCount++;
                    } catch (Exception ex) {
                        // per-outbox processing failed; EventProcessorService already persisted a failure row and scheduled retry
                        failureCount++;
                        log.warn("Outbox processing failed for outboxId={} in run {}: {}", outbox.getOutboxId(), runId, ex.getMessage());
                    }
                }
            }

            // finalize run
            etlRunService.finishRun(EtlRunCompletion.builder().etlRunId(runId).status("finished").submittedCount(submittedCount)
                .successCount(successCount).failureCount(failureCount).minSubmissionSerial(minSerial).maxSubmissionSerial(maxSerial)
                .build());
            log.info("Finished run {}: submitted={}, success={}, failure={}", runId, submittedCount, successCount, failureCount);
        } catch (Exception topEx) {
            // best-effort mark run as failed
            try {
                etlRunService.finishRun(EtlRunCompletion.builder().etlRunId(runId).status("failed").submittedCount(submittedCount)
                    .successCount(successCount).failureCount(failureCount).minSubmissionSerial(minSerial).maxSubmissionSerial(maxSerial)
                    .build());
            } catch (Exception e) {
                log.error("Failed to update run status for run {}", runId, e);
            }
            log.error("Orchestration error for run {}: {}", runId, topEx.getMessage(), topEx);
            throw new RuntimeException(topEx);
        }
    }
}

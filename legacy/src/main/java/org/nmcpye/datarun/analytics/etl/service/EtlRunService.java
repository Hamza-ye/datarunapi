package org.nmcpye.datarun.analytics.etl.service;

import org.nmcpye.datarun.analytics.etl.dto.EtlRunCompletion;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;

import java.util.UUID;

/**
 * Service interface managing ETL run lifecycle.
 * Responsibilities:
 * - create/start run
 * - update run counters and status during/after processing
 * - finish run
 * - query runs for monitoring and reprocessing decisions
 */
public interface EtlRunService {

    /**
     * Create and persist a new EtlRun record with status 'running'.
     * Returns the persisted EtlRun (with generated etlRunId).
     */
    EtlRun startRun(String pipelineName, String createdBy, String codeVersion);

    /**
     * Mark run as finished and persist counts and status.
     * Implementation should set finishedAt and update counters.
     */
    @SuppressWarnings("UnusedReturnValue")
    EtlRun finishRun(EtlRunCompletion etlRunCompletion);

    /**
     * Update counters (incremental) during a run.
     * Implementation should be resilient to concurrent updates if needed.
     */
    void incrementSuccess(UUID etlRunId, int delta);

    void incrementFailure(UUID etlRunId, int delta);
}

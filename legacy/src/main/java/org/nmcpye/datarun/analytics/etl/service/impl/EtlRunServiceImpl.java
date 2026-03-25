package org.nmcpye.datarun.analytics.etl.service.impl;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.dto.EtlRunCompletion;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;
import org.nmcpye.datarun.analytics.etl.repository.EtlRunRepository;
import org.nmcpye.datarun.analytics.etl.service.EtlRunService;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * EtlRunService implementation.
 * <p>
 * Responsibilities implemented here:
 * - startRun / finishRun (basic skeleton persisted via JPA repository)
 * - atomic DB-side increments for success and failure counters using NamedParameterJdbcTemplate
 * <p>
 * Concurrency & atomicity notes:
 * - incrementSuccess/incrementFailure execute a single UPDATE that increments the counter atomically
 * in the database, avoiding lost updates under concurrent callers.
 * - Relying on DB-side arithmetic ensures correctness without loading the entity first.
 */
@Service
@RequiredArgsConstructor
public class EtlRunServiceImpl implements EtlRunService {

    private static final String INCREMENT_SUCCESS_SQL =
        "UPDATE etl_runs SET success_count = COALESCE(success_count, 0) + :delta WHERE etl_run_id = :id";

    private static final String INCREMENT_FAILURE_SQL =
        "UPDATE etl_runs SET failure_count = COALESCE(failure_count, 0) + :delta WHERE etl_run_id = :id";

    private final EtlRunRepository etlRunRepository;
    private final NamedParameterJdbcTemplate jdbc;
    private final Environment env;

    @Override
    @Transactional
    public EtlRun startRun(String pipelineName, String createdBy, String codeVersion) {
        EtlRun run = EtlRun.builder()
            .etlRunId(UUID.randomUUID())
            .pipelineName(pipelineName)
            .startedAt(Instant.now())
            .status("running")
            .submittedCount(0L)
            .successCount(0L)
            .failureCount(0L)
            .createdBy(createdBy)
            .metadata("codeVersion", codeVersion)
            .metadata("activeProfile", env.getProperty("spring.profiles.active"))
            .metadata("projectVersion", env.getProperty("spring-boot.formatted-version"))
            .build();

        return etlRunRepository.persist(run);
    }

    @Override
    @Transactional
    public EtlRun finishRun(EtlRunCompletion etlRunCompletion) {
        EtlRun run = etlRunRepository.findById(etlRunCompletion.etlRunId())
            .orElseThrow(() -> new IllegalArgumentException("etl run not found: " + etlRunCompletion.etlRunId()));
        run.setFinishedAt(Instant.now());
        run.setStatus(etlRunCompletion.status());
        run.setSubmittedCount(etlRunCompletion.submittedCount());
        run.setSuccessCount(etlRunCompletion.successCount());
        run.setFailureCount(etlRunCompletion.failureCount());
        run.setMinSubmissionSerial(etlRunCompletion.minSubmissionSerial());
        run.setMaxSubmissionSerial(etlRunCompletion.maxSubmissionSerial());
        return etlRunRepository.persist(run);
    }

    /**
     * Atomically increment the success_count by delta at the DB level.
     * Uses a single UPDATE statement to avoid read-modify-write races.
     */
    @Override
    @Transactional
    public void incrementSuccess(UUID etlRunId, int delta) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("delta", delta)
            .addValue("id", etlRunId);
        int updated = jdbc.update(INCREMENT_SUCCESS_SQL, params);
        if (updated == 0) {
            // No row updated: caller might have passed an invalid etlRunId; surface as IllegalStateException
            throw new IllegalStateException("incrementSuccess: no etl_runs row found for id=" + etlRunId);
        }
    }

    /**
     * Atomically increment the failure_count by delta at the DB level.
     * Uses a single UPDATE statement to avoid read-modify-write races.
     */
    @Override
    @Transactional
    public void incrementFailure(UUID etlRunId, int delta) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("delta", delta)
            .addValue("id", etlRunId);
        int updated = jdbc.update(INCREMENT_FAILURE_SQL, params);
        if (updated == 0) {
            // No row updated: caller might have passed an invalid etlRunId; surface as IllegalStateException
            throw new IllegalStateException("incrementFailure: no etl_runs row found for id=" + etlRunId);
        }
    }
}

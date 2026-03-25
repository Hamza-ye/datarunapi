package org.nmcpye.datarun.outbox.service.impl;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.service.EtlRunService;
import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.nmcpye.datarun.outbox.dto.OutboxProcessingDto;
import org.nmcpye.datarun.outbox.entity.OutboxProcessing;
import org.nmcpye.datarun.outbox.repository.OutboxClaimPort;
import org.nmcpye.datarun.outbox.repository.OutboxProcessingJdbcRepository;
import org.nmcpye.datarun.outbox.service.OutboxProcessingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service responsible for recording processing outcomes and updating outbox
 * status.
 * <p>
 * Future:
 * - ensure operations are idempotent: if recordSuccess called twice, it's no-op
 * - transaction boundaries: the write to tall table and the processing record
 * should be coordinated
 * (for now orchestrator will decide whether to make them part of a single
 * transaction)
 */
@Service
@RequiredArgsConstructor
public class OutboxProcessingServiceImpl implements OutboxProcessingService {
    private final OutboxProcessingJdbcRepository processingJdbcRepository;
    private final OutboxClaimPort outboxJdbcRepository;
    private final EtlRunService etlRunService;

    @Override
    @Transactional
    public OutboxProcessing startProcessing(UUID etlRunId, OutboxDto outbox) {
        OutboxProcessingDto dto = OutboxProcessingDto.builder()
                .etlRunId(etlRunId)
                .outboxId(outbox.getOutboxId())
                .status("processing")
                .submissionId(outbox.getSubmissionId())
                .submissionUid(outbox.getSubmissionUid())
                .submissionSerialNumber(outbox.getSubmissionSerialNumber())
                .attempt(outbox.getAttempt())
                .processedAt(Instant.now())
                .build();
        return processingJdbcRepository.upsertOutboxProcessing(dto);
    }

    /**
     * Record success for the outbox row.
     * <p>
     * Transactional & idempotency behavior:
     * - This method is transactional: inserted outbox_processing and outbox table
     * update happen in one TX.
     * - If a success row for (etlRunId, outboxId) already exists, we do nothing
     * (idempotent).
     * - We insert the success bookkeeping row and THEN update the outbox table to
     * 'success'.
     * <p>
     * Reasoning: inserting the processing row first gives us a durable audit entry;
     * joining the repository
     * transaction means if markOutboxSuccess fails the TX will rollback and no
     * partial state remains.
     */
    @Override
    @Transactional
    public void recordSuccess(UUID etlRunId, OutboxDto outbox) {
        OutboxProcessingDto dto = OutboxProcessingDto.builder()
                .etlRunId(etlRunId)
                .outboxId(outbox.getOutboxId())
                .status("success")
                .submissionId(outbox.getSubmissionId())
                .submissionUid(outbox.getSubmissionUid())
                .submissionSerialNumber(outbox.getSubmissionSerialNumber())
                .attempt(outbox.getAttempt())
                .processedAt(Instant.now())
                .error(null)
                .build();

        processingJdbcRepository.upsertOutboxProcessing(dto);

        // update outbox status to 'success' (joins same transaction)
        outboxJdbcRepository.markSuccess(outbox.getOutboxId());

        // increment run-level success counter
        etlRunService.incrementSuccess(etlRunId, 1);
    }

    /**
     * Record failure for the outbox row.
     * <p>
     * Transactional behavior and safety:
     * - This method is transactional: insertion into outbox_processing and the call
     * that updates the outbox
     * row are executed in the same transaction so either both succeed or both roll
     * back.
     * - We truncate/normalize the error message to avoid storing huge traces in the
     * DB.
     * <p>
     * Backoff & scheduling:
     * - nextAttemptAt is provided by the caller and can be computed (exponential
     * backoff) outside this method.
     * - If markOutboxFailure returns no rows (or throws), we fail fast by throwing
     * IllegalStateException so
     * the caller can decide on retry semantics.
     */
    @Override
    @Transactional
    public void recordFailure(UUID etlRunId, OutboxDto outbox, String error, Instant nextAttemptAt) {
        final int ERROR_MAX_LEN = 1000;
        String safeError = (error == null) ? "unspecified error" : error;
        if (safeError.length() > ERROR_MAX_LEN) {
            safeError = safeError.substring(0, ERROR_MAX_LEN) + "...(truncated)";
        }

        // insert failure processing record (will be rolled back if markOutboxFailure
        // fails)
        OutboxProcessingDto failureRecord = OutboxProcessingDto.builder()
                .etlRunId(etlRunId)
                .outboxId(outbox.getOutboxId())
                .submissionSerialNumber(outbox.getSubmissionSerialNumber())
                .submissionUid(outbox.getSubmissionUid())
                .submissionId(outbox.getSubmissionId())
                .status("failure")
                .attempt(outbox.getAttempt())
                .error(safeError)
                .processedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        processingJdbcRepository.upsertOutboxProcessing(failureRecord);
    }

    // @Override
    // @Transactional
    // public void recordFailure(UUID etlRunId, OutboxDto outbox, String error,
    // Instant nextAttemptAt) {
    // // sanitize / truncate error message to avoid storing huge stack traces
    // (e.g., cap at 1000 chars)
    //// final int ERROR_MAX_LEN = 1000;
    // String safeError = (error == null) ? "unspecified error" : error;
    // if (safeError.length() > EtlConstants.ERROR_MAX) {
    // safeError = safeError.substring(0, EtlConstants.ERROR_MAX) +
    // "...(truncated)";
    // }
    //
    // Optional<OutboxProcessing> existing =
    // processingRepository.findByEtlRunIdAndOutboxId(etlRunId,
    // outbox.getOutboxId());
    //
    // if (existing.isPresent()) {
    // OutboxProcessing p = existing.get();
    // p.setStatus("failure");
    // p.setAttempt(outbox.getAttempt());
    // p.setError(safeError);
    // p.setProcessedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    // processingRepository.updateAndFlush(p);
    // } else {
    // OutboxProcessing successRecord = OutboxProcessing.builder()
    // .etlRunId(etlRunId)
    // .outboxId(outbox.getOutboxId())
    // .submissionSerialNumber(outbox.getSubmissionSerialNumber())
    // .submissionUid(outbox.getSubmissionUid())
    // .submissionId(outbox.getSubmissionId())
    // .status("failure")
    // .attempt(outbox.getAttempt())
    // .error(safeError)
    // .processedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
    // .build();
    // try {
    // processingRepository.persistAndFlush(successRecord);
    // } catch (DataIntegrityViolationException dive) {
    // // concurrent insert happened — fall back to update
    // Optional<OutboxProcessing> fallback =
    // processingRepository.findByEtlRunIdAndOutboxId(etlRunId,
    // outbox.getOutboxId());
    // if (fallback.isPresent()) {
    // OutboxProcessing p = fallback.get();
    // p.setStatus("failure");
    // p.setAttempt(outbox.getAttempt());
    // p.setError(safeError);
    // p.setProcessedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    // processingRepository.updateAndFlush(p);
    // } else {
    // throw dive; // unexpected — rethrow
    // }
    // }
    // }
    //
    // // insert failure processing record (will be rolled back if markOutboxFailure
    // fails)
    // OutboxProcessing failureRecord = OutboxProcessing.builder()
    // .etlRunId(etlRunId)
    // .outboxId(outbox.getOutboxId())
    // .submissionSerialNumber(outbox.getSubmissionSerialNumber())
    // .submissionUid(outbox.getSubmissionUid())
    // .submissionId(outbox.getSubmissionId())
    // .status("failure")
    // .attempt(outbox.getAttempt())
    // .error(safeError)
    // .processedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
    // .build();
    //
    // try {
    // processingRepository.persist(failureRecord);
    // } catch (DataIntegrityViolationException dive) {
    // // concurrent insertion (same (etl_run_id, outbox_id)) - proceed to ensure
    // outbox row is updated.
    // // The unique constraint ensures only one bookkeeping row per run+outbox;
    // duplicate attempts are safe.
    // }
    //
    // // call JDBC repo to update outbox attempt/last_error/next_attempt_at and
    // capture the new attempt count
    // Optional<Integer> attemptOpt =
    // outboxJdbcRepository.markOutboxFailure(outbox.getOutboxId(), safeError,
    // nextAttemptAt);
    //
    // if (attemptOpt.isEmpty()) {
    // // markOutboxFailure did not update any row -> inconsistent state; surface to
    // caller for retry handling
    // throw new IllegalStateException("markOutboxFailure did not affect outbox row
    // for id=" + outbox.getOutboxId());
    // }
    //
    // // increment run-level failure counter
    // etlRunService.incrementFailure(etlRunId, 1);
    // }

    // @Override
    // @Transactional
    // public void recordFailure(UUID etlRunId, OutboxDto outbox, String error,
    // Instant nextAttemptAt) {
    // // sanitize / truncate error message to avoid storing huge stack traces
    // (e.g., cap at 1000 chars)
    // final int ERROR_MAX_LEN = 1000;
    // String safeError = (error == null) ? "unspecified error" : error;
    // if (safeError.length() > ERROR_MAX_LEN) {
    // safeError = safeError.substring(0, ERROR_MAX_LEN) + "...(truncated)";
    // }
    //
    // Optional<OutboxProcessing> existing =
    // outboxProcessingRepository.findByEtlRunIdAndOutboxId(etlRunId,
    // outbox.getOutboxId());
    //
    // // insert failure processing record (will be rolled back if markOutboxFailure
    // fails)
    // OutboxProcessing failureRecord = OutboxProcessing.builder()
    // .etlRunId(etlRunId)
    // .outboxId(outbox.getOutboxId())
    // .submissionSerialNumber(outbox.getSubmissionSerialNumber())
    // .submissionUid(outbox.getSubmissionUid())
    // .submissionId(outbox.getSubmissionId())
    // .status("failure")
    // .attempt(outbox.getAttempt())
    // .error(safeError)
    // .processedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
    // .build();
    //
    // try {
    // outboxProcessingRepository.persist(failureRecord);
    // } catch (DataIntegrityViolationException dive) {
    // // concurrent insertion (same (etl_run_id, outbox_id)) - proceed to ensure
    // outbox row is updated.
    // // The unique constraint ensures only one bookkeeping row per run+outbox;
    // duplicate attempts are safe.
    // }
    //
    // // call JDBC repo to update outbox attempt/last_error/next_attempt_at and
    // capture the new attempt count
    // Optional<Integer> attemptOpt =
    // outboxJdbcRepository.markOutboxFailure(outbox.getOutboxId(), safeError,
    // nextAttemptAt);
    //
    // if (attemptOpt.isEmpty()) {
    // // markOutboxFailure did not update any row -> inconsistent state; surface to
    // caller for retry handling
    // throw new IllegalStateException("markOutboxFailure did not affect outbox row
    // for id=" + outbox.getOutboxId());
    // }
    //
    // // increment run-level failure counter
    // etlRunService.incrementFailure(etlRunId, 1);
    // }
}

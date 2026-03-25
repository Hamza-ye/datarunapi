package org.nmcpye.datarun.outbox.repository;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.outbox.dto.OutboxProcessingDto;
import org.nmcpye.datarun.outbox.entity.OutboxProcessing;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OutboxProcessingJdbcRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private static final RowMapper<OutboxProcessing> OUTBOX_PROCESSING_ROW_MAPPER = (rs, rowNum) ->
        OutboxProcessing.builder()
            .etlRunId(rs.getObject("etl_run_id", UUID.class))
            .outboxId(rs.getLong("outbox_id"))
            .submissionSerialNumber(rs.getObject("submission_serial_number") == null ? null : rs.getLong("submission_serial_number"))
            .submissionId(rs.getString("submission_id"))
            .submissionUid(rs.getString("submission_uid"))
            .status(rs.getString("status"))
            .attempt(rs.getInt("attempt"))
            .error(rs.getString("error"))
            .processedAt(rs.getTimestamp("processed_at") != null ? rs.getTimestamp("processed_at").toInstant() : null)
            .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
            .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
            .build();

    // NOTE: removed explicit CAST and rely on binding a java.util.UUID
    private static final String SQL = """
        INSERT INTO outbox_processing (
          etl_run_id,
          outbox_id,
          submission_serial_number,
          submission_id,
          submission_uid,
          status,
          attempt,
          error,
          processed_at,
          created_at,
          updated_at
        )
        VALUES (
          :etlRunId,
          :outboxId,
          :submissionSerialNumber,
          :submissionId,
          :submissionUid,
          :status,
          :attempt,
          :error,
          :processedAt,
          now(),
          now()
        )
        ON CONFLICT (etl_run_id, outbox_id) DO UPDATE SET
          status = EXCLUDED.status,
          attempt = EXCLUDED.attempt,
          error = EXCLUDED.error,
          processed_at = EXCLUDED.processed_at,
          updated_at = now()
        RETURNING
          etl_run_id,
          outbox_id,
          submission_serial_number,
          submission_id,
          submission_uid,
          status,
          attempt,
          error,
          processed_at,
          created_at,
          updated_at;
        """;

    /**
     * Upsert an outbox_processing bookkeeping row and return the persisted row (inserted or updated).
     * Caller should ensure strings passed (e.g. error) are not sensitive / or already sanitized.
     */
    public OutboxProcessing upsertOutboxProcessing(OutboxProcessingDto dto) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("etlRunId", dto.etlRunId()) // bind UUID directly
            .addValue("outboxId", dto.outboxId())
            .addValue("submissionSerialNumber", dto.submissionSerialNumber())
            .addValue("submissionId", dto.submissionId())
            .addValue("submissionUid", dto.submissionUid())
            .addValue("status", dto.status())
            .addValue("attempt", dto.attempt())
            .addValue("error", dto.error())
            .addValue("processedAt", dto.processedAt() == null ? null : java.sql.Timestamp.from(dto.processedAt()));

        try {
            return jdbc.queryForObject(SQL, params, OUTBOX_PROCESSING_ROW_MAPPER);
        } catch (EmptyResultDataAccessException erdae) {
            // Shouldn't happen because we RETURNING the row — make it explicit
            throw new IllegalStateException("OutboxProcessing upsert returned no row (etlRun=" + dto.etlRunId() + ", outbox=" + dto.outboxId() + ")", erdae);
        } catch (DataAccessException dae) {
            // bubble up (service layer can decide retry/backoff). Could wrap if you want.
            throw dae;
        }
    }
}

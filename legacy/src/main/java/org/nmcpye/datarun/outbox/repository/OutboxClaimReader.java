package org.nmcpye.datarun.outbox.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.outbox.dto.OutboxDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OutboxClaimReader implements OutboxClaimPort {

    private final NamedParameterJdbcTemplate cloudJdbc;

    private static final String CLAIM_SQL =
        "WITH cte AS (" +
            "  SELECT outbox_id" +
            "  FROM outbox " +
            "  WHERE status = 'pending' " +
            "    AND (next_attempt_at IS NULL OR next_attempt_at <= now()) " +
            "  ORDER BY submission_serial_number " +
            "  FOR UPDATE SKIP LOCKED " +
            "  LIMIT :limit " +
            ") " +
            "UPDATE outbox " +
            "SET ingest_id = :ingestId, claimed_at= now(), claimed_by= :claimedBy " +
            "WHERE outbox_id IN (SELECT outbox_id FROM cte) " +
            "RETURNING outbox.*;";

    private static final String MARK_SUCCESS_SQL =
        "UPDATE outbox " +
            "SET status = 'success', last_error = NULL, next_attempt_at = NULL " +
            "WHERE outbox_id = :outboxId;";

    private static final String MARK_SUCCESS_IF_INGEST_SQL =
        "UPDATE outbox " +
            "SET status = 'success', last_error = NULL, next_attempt_at = NULL " +
            "WHERE outbox_id = :outboxId AND ingest_id = :ingestId;";

    private static final String MARK_FAILURE_SQL_RETURNING =
        "UPDATE outbox " +
            "SET attempt = COALESCE(attempt, 0) + 1, " +
            "    last_error = :error, " +
            "    next_attempt_at = :nextAttemptAt, " +
            "    status = 'failed' " +
            "WHERE outbox_id = :outboxId " +
            "RETURNING attempt;";

    private static final RowMapper<OutboxDto> OUTBOX_ROW_MAPPER = new RowMapper<>() {
        @Override
        public OutboxDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return OutboxDto.builder()
                .outboxId(rs.getLong("outbox_id"))
                .submissionSerialNumber(rs.getLong("submission_serial_number"))
                .submissionId(rs.getString("submission_id"))
                .submissionUid(rs.getString("submission_uid"))
                .eventType(rs.getString("event_type"))
                .topic(rs.getString("topic"))
                .payload(rs.getString("payload"))
                .status(rs.getString("status"))
                .attempt(rs.getInt("attempt"))
                .claimedBy(rs.getString("claimed_by"))
                .claimedAt(rs.getTimestamp("claimed_at") != null ? rs.getTimestamp("claimed_at").toInstant() : null)
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
                .ingestId(rs.getObject("ingest_id", UUID.class))
                .build();
        }
    };

    @Override
    @Transactional
    public List<OutboxDto> claimPendingBatch(UUID ingestId, String claimedBy, int limit) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("claimedBy", claimedBy)
            .addValue("ingestId", ingestId)
            .addValue("limit", limit);

        return cloudJdbc.query(CLAIM_SQL, params, OUTBOX_ROW_MAPPER);
    }

    // --- claim processor

    @Override
    @Transactional
    public void markSuccess(long outboxId) {
        int updated = cloudJdbc.update(MARK_SUCCESS_SQL, new MapSqlParameterSource("outboxId", outboxId));
        if (updated == 0) {
            throw new IllegalStateException("markSuccess: no outbox row updated for id=" + outboxId);
        }
    }

    @Override
    @Transactional
    public boolean markSuccessIfIngestMatches(long outboxId, UUID ingestId) {
        int updated = cloudJdbc.update(
            MARK_SUCCESS_IF_INGEST_SQL,
            new MapSqlParameterSource()
                .addValue("outboxId", outboxId)
                .addValue("ingestId", ingestId)
        );
        return updated > 0;
    }

    @Override
    @jakarta.transaction.Transactional
    public int markFailure(long outboxId, String error, Instant nextAttemptAt) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("outboxId", outboxId)
            .addValue("error", error == null ? null : error.substring(0, Math.min(4000, error.length())))
            .addValue("nextAttemptAt", nextAttemptAt == null ? null : Timestamp.from(nextAttemptAt));

        List<Integer> attempts = cloudJdbc.query(
            MARK_FAILURE_SQL_RETURNING,
            params,
            (rs, rowNum) -> rs.getInt("attempt")
        );

        if (attempts.isEmpty()) {
            throw new IllegalStateException("markFailure: no outbox row updated for id=" + outboxId);
        }
        return attempts.get(0);
    }
}


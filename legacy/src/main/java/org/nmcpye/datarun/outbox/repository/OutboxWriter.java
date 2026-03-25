package org.nmcpye.datarun.outbox.repository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class OutboxWriter implements OutboxWritePort {

    private final NamedParameterJdbcTemplate jdbc;

    // --- Inserts (reused SQL from previous) ---

    @Override
    @Transactional
    public int insertBackfillIfNotExists(List<OutboxWritePort.OutboxInsert> inserts) {
        if (inserts == null || inserts.isEmpty())
            return 0;

        final String sql = ""
                + " INSERT INTO outbox ("
                + " submission_id, submission_uid, topic, payload, event_type, status, attempt, created_at, submission_serial_number, correlation_id, occurred_at"
                + ") VALUES ("
                + " :submission_id, :submission_uid, :topic, cast(:payload AS jsonb), 'BACKFILL', 'pending', 0, :created_at, :submission_serial_number, :correlation_id, :occurred_at"
                + ") ON CONFLICT (submission_id) WHERE (event_type = 'BACKFILL') DO NOTHING";

        return batchInsert(sql, inserts, null);
    }

    @Override
    @Transactional
    public int insertByEventType(List<OutboxWritePort.OutboxInsert> inserts, String eventType) {
        if (inserts == null || inserts.isEmpty())
            return 0;

        final String sql = """
                  INSERT INTO outbox (
                submission_id, submission_serial_number, submission_uid, topic, payload,
                                      event_type, status, attempt, created_at, correlation_id, occurred_at)
                VALUES (:submission_id, :submission_serial_number, :submission_uid, :topic, cast(:payload AS jsonb),
                :event_type, 'pending', 0, :created_at, :correlation_id, :occurred_at)
                """;

        return batchInsert(sql, inserts, eventType);
    }

    private int batchInsert(String sql, List<OutboxWritePort.OutboxInsert> inserts, String eventType) {
        MapSqlParameterSource[] batch = inserts.stream().map(it -> {
            MapSqlParameterSource p = new MapSqlParameterSource();
            p.addValue("submission_id", it.submissionId);
            p.addValue("submission_uid", it.submissionUid);
            p.addValue("submission_serial_number", it.submissionSerialNumber);
            p.addValue("topic", it.templateVersionUid);
            p.addValue("payload", it.payload);
            p.addValue("created_at", Timestamp.from(it.createdAt));
            p.addValue("correlation_id", it.correlationId);
            p.addValue("occurred_at", it.occurredAt != null ? Timestamp.from(it.occurredAt) : null);
            if (eventType != null)
                p.addValue("event_type", eventType);
            return p;
        }).toArray(MapSqlParameterSource[]::new);

        int[] results = jdbc.batchUpdate(sql, batch);
        int inserted = (int) Arrays.stream(results).filter(r -> r > 0).count();
        log.info("OutboxWriter: attempted {}, inserted {}", inserts.size(), inserted);
        return inserted;
    }
}

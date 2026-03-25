package org.nmcpye.datarun.outbox.repository;

import java.time.Instant;
import java.util.List;

public interface OutboxWritePort {
    /**
     * Insert backfill rows if not exists (returns number inserted).
     */
    int insertBackfillIfNotExists(List<OutboxInsert> inserts);

    /**
     * Generic insert by event type.
     */
    int insertByEventType(List<OutboxInsert> inserts, String eventType);

    class OutboxInsert {
        public final String submissionId;
        public final String submissionUid;
        public final String templateVersionUid;
        public final String payload;
        public final Instant createdAt;
        public final Long submissionSerialNumber;
        public final String correlationId;
        public final Instant occurredAt;

        public OutboxInsert(String submissionId,
                String submissionUid,
                String templateVersionUid,
                String payload,
                Instant createdAt,
                Long submissionSerialNumber,
                String correlationId,
                Instant occurredAt) {
            this.submissionId = submissionId;
            this.submissionUid = submissionUid;
            this.templateVersionUid = templateVersionUid;
            this.payload = payload;
            this.createdAt = createdAt;
            this.submissionSerialNumber = submissionSerialNumber;
            this.correlationId = correlationId;
            this.occurredAt = occurredAt;
        }
    }
}

package org.nmcpye.datarun.outbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity mapping of outbox. This entity is intentionally minimal;
 * high-throughput claim/update operations should be done via
 * NamedParameterJdbcTemplate for
 * better control over "UPDATE ... RETURNING" queries (see
 * OutboxJdbcRepository).
 * <p>
 * Future work:
 * - add optimistic locking, if needed
 * - consider JSON<->Object mapping for payload
 */
@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxId;

    // Reference to DataSubmission.serial_number (DB sequence value)
    @Column(name = "submission_serial_number", nullable = false)
    private Long submissionSerialNumber;

    @Column(name = "submission_id", nullable = false)
    private String submissionId;

    @Column(name = "submission_uid", nullable = false, length = 11)
    private String submissionUid;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    /**
     * DataSubmission ingest
     */
    @Column(name = "topic", nullable = false)
    private String topic;

    /**
     * Update, Create, Delete
     */
    @Column(name = "event_type", length = 128)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @Column(name = "status")
    private String status;

    @Column(name = "attempt")
    private Integer attempt;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;

    @Column(name = "ingest_id")
    private UUID ingestId;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "claimed_by")
    private String claimedBy;

    @Column(name = "occurred_at")
    private Instant occurredAt;

    @Column(name = "created_at")
    private Instant createdAt;
}

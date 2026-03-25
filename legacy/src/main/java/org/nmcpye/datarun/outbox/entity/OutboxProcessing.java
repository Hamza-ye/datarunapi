package org.nmcpye.datarun.outbox.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Per-outbox processing audit. We keep entries for each processed outbox row and the run it belonged to.
 * <p>
 * Future work:
 * - consider storing structured error details (JSON)
 * - add indices for etl_run_id to quickly fetch failures for re-run
 */
@Entity
@Table(name = "outbox_processing", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"etl_run_id", "outbox_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxProcessing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etl_run_id")
    private UUID etlRunId;

    @Column(name = "outbox_id")
    private Long outboxId;

    // Reference to DataSubmission.serial_number (DB sequence value)
    @Column(name = "submission_serial_number")
    private Long submissionSerialNumber;

    @Column(name = "submission_id")
    private String submissionId;

    @Column(name = "submission_uid")
    private String submissionUid;

    @Column(name = "status")
    private String status; // processing/success/failure

    @Column(name = "attempt")
    private Integer attempt;

    @Column(name = "error", columnDefinition = "text")
    private String error;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
}

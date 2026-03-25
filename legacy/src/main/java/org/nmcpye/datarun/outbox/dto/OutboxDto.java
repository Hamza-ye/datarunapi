package org.nmcpye.datarun.outbox.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboxDto {
    private Long outboxId;
    // Reference to DataSubmission.serial_number (DB sequence value)
    private Long submissionSerialNumber;

    private String submissionId;
    private String submissionUid;
    private String topic;
    private String eventType;
    private String correlationId;
    private String payload; // JSON payload as String (maps to JSONB)
    private String status;
    private Integer attempt;
    private Instant occurredAt;
    private Instant createdAt;
    private String claimedBy;
    private Instant claimedAt;
    private UUID ingestId; // assigned at claim time
    private String lastError;
    private Instant nextAttemptAt;
}

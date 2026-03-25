package org.nmcpye.datarun.outbox.dto;

import lombok.Builder;
import org.nmcpye.datarun.outbox.entity.OutboxProcessing;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link OutboxProcessing}
 */
@Builder
public record OutboxProcessingDto(UUID etlRunId, Long outboxId, Long submissionSerialNumber, String submissionId,
                                  String submissionUid, String status, Integer attempt, String error,
                                  Instant processedAt) implements Serializable {
}

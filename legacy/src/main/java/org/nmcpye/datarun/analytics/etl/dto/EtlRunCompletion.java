package org.nmcpye.datarun.analytics.etl.dto;

import lombok.Builder;
import lombok.experimental.Accessors;
import org.nmcpye.datarun.analytics.etl.entity.EtlRun;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link EtlRun}
 */
@Builder
@Accessors(chain=true)
public record EtlRunCompletion(UUID etlRunId, String status, Long minSubmissionSerial,
                               Long maxSubmissionSerial, Long submittedCount, Long successCount, Long failureCount,
                               String watermark, String metadata) implements Serializable {
}

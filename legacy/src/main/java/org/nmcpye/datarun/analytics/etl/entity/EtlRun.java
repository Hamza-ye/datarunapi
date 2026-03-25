package org.nmcpye.datarun.analytics.etl.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * JPA entity for the etl_runs table.
 * Minimal mapping now; metadata and watermark are stored as JSON strings for now.
 * <p>
 * Future work:
 * - store metadata as JsonNode with hibernate-types
 * - add indexes for pipeline_name + started_at
 */
@Entity
@Table(name = "etl_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtlRun {

    @Id
    @Column(name = "etl_run_id", nullable = false, updatable = false)
    private UUID etlRunId;

    @Column(name = "pipeline_name", nullable = false)
    private String pipelineName;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "status")
    private String status; // running/success/partial_fail/failed

    @Column(name = "min_submission_serial")
    private Long minSubmissionSerial;

    @Column(name = "max_submission_serial")
    private Long maxSubmissionSerial;

    @Column(name = "submitted_count")
    private Long submittedCount;

    @Column(name = "success_count")
    private Long successCount;

    @Column(name = "failure_count")
    private Long failureCount;

    @Column(name = "watermark", columnDefinition = "jsonb default '{}'::jsonb")
    @Type(JsonType.class)
    private Map<String, Object> watermark; // JSON (string) for now

    @Singular("metadata")
    @Column(name = "metadata", columnDefinition = "jsonb default '{}'::jsonb")
    @Type(JsonType.class)
    private Map<String, Object> metadata; // JSON (string) for now

    @Column(name = "created_by")
    private String createdBy;
}

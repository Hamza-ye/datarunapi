package org.nmcpye.platform.core.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record of something that happened in the platform.
 * The audit backbone and integration mechanism.
 *
 * Once created, events are never modified or deleted.
 */
@Getter
@Setter
@NoArgsConstructor
@jakarta.persistence.Entity
@Table(name = "platform_event",
       indexes = {
           @Index(name = "idx_event_subject", columnList = "subject_type, subject_id"),
           @Index(name = "idx_event_timestamp", columnList = "timestamp"),
           @Index(name = "idx_event_correlation", columnList = "correlation_id")
       })
public class PlatformEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Event type (e.g., "entity.created", "entity.updated", "flow_task.submitted").
     */
    @NotBlank
    @Size(max = 100)
    @Column(name = "type", nullable = false, length = 100)
    private String type;

    /**
     * Type of the subject this event is about (e.g., "entity", "flow_task").
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "subject_type", nullable = false, length = 50)
    private String subjectType;

    /**
     * ID of the subject this event is about.
     */
    @NotNull
    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    /**
     * ID of the actor who caused this event.
     */
    @Column(name = "actor_id")
    private UUID actorId;

    @NotNull
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    /**
     * Optional correlation ID for grouping related events.
     */
    @Column(name = "correlation_id")
    private UUID correlationId;

    /**
     * Additional event data, stored as JSONB.
     */
    @Column(name = "payload", columnDefinition = "jsonb")
    private String payload;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}

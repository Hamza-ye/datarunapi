package org.nmcpye.datarun.analytics.etl.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "events", schema = "analytics")
public class EventInstance {
    @Id
    @Size(max = 26)
    @Column(name = "event_id", nullable = false, length = 26)
    private String eventId;

    @Column(name = "submission_serial")
    private Long submissionSerial;

    @Size(max = 20)
    @Column(name = "event_type", length = 20)
    private String eventType;

    @Size(max = 11)
    @Column(name = "submission_uid", length = 11)
    private String submissionUid;

    @Size(max = 26)
    @Column(name = "submission_id", length = 26)
    private String submissionId;

    @Size(max = 26)
    @Column(name = "parent_event_id", length = 26)
    private String parentEventId;

    @Column(name = "event_ce_id")
    private UUID eventCeId;

    @Size(max = 11)
    @Column(name = "assignment_uid", length = 11)
    private String assignmentUid;

    @Size(max = 11)
    @Column(name = "activity_uid", length = 11)
    private String activityUid;

    @Size(max = 11)
    @Column(name = "org_unit_uid", length = 11)
    private String orgUnitUid;

    @Size(max = 11)
    @Column(name = "team_uid", length = 11)
    private String teamUid;

    @Size(max = 11)
    @Column(name = "template_uid", length = 11)
    private String templateUid;

    @Column(name = "submission_creation_time")
    private Instant submissionCreationTime;

    @Column(name = "start_time")
    private Instant startTime;

    @ColumnDefault("now()")
    @Column(name = "created_at")
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Size(max = 255)
    @Column(name = "created_by")
    private String createdBy;

    @Size(max = 255)
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}

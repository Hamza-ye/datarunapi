package org.nmcpye.datarun.datacapture.datasubmission;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import org.nmcpye.datarun.sharedkernal.JpaSoftDeleteObject;
import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * A DataSubmission, a Data instance versioned body, containing the actual submission data.
 *
 * @author Hamza Assada
 * @since 14/08/2025
 */
@Entity
@Table(
    name = "data_submission",
    indexes = {
        @Index(name = "idx_ds_form_assignment", columnList = "template_uid, assignment_uid"),
        @Index(name = "idx_ds_serial_number", columnList = "serial_number"),
        @Index(name = "idx_ds_orgunit", columnList = "org_unit_uid"),
    }
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DataSubmission extends JpaSoftDeleteObject {
    /**
     * DB-generated serial (bigint sequence + unique).
     * Keep insertable=false if DB default nextval() used.
     */
    @Column(name = "serial_number", unique = true, insertable = false, updatable = false)
    @Generated(event = EventType.INSERT)
    private Long serialNumber;

    @Size(max = 11, message = "uid should be of size 11 max")
    @Column(name = "uid", length = 11, unique = true, nullable = false)
    private String uid;

    private Boolean deleted = false;
    private Instant deletedAt;

    /**
     * Flexible JSONB payload
     */
    @Column(name = "form_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode formData;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private FlowStatus status;

    @NotNull(message = "form uid can't be null")
    @Column(name = "template_uid", nullable = false, updatable = false)
    private String form;

    @Column(name = "template_version_uid", updatable = false)
    private String formVersion;

    @Column(name = "template_version_no", updatable = false)
    private Integer version;

    @Column(name = "team_uid")
    private String team;

    @Column(name = "team_code")
    private String teamCode;

    @Column(name = "org_unit_uid")
    private String orgUnit;

    @Column(name = "org_unit_code")
    private String orgUnitCode;

    @Column(name = "org_unit_name")
    private String orgUnitName;

    @Column(name = "activity_uid")
    private String activity;

    @Column(name = "assignment_uid")
    private String assignment;

    @Column(name = "start_entry_time")
    private Instant startEntryTime;

    @Column(name = "finished_entry_time")
    private Instant finishedEntryTime;

    /**
     * Optimistic lock field — use this for reliable commitVersion in audit/history.
     */
    @Version
    @Column(name = "lock_version")
    @JsonIgnore
    private Long lockVersion;


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DataSubmission that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }

    @Override
    public String toString() {
        return "DataSubmission{id=" + id + ", uid=" + uid + ", form=" + form + ", serialNumber=" + serialNumber + "}";
    }

    @JsonIgnore
    public String getId() {
        return id;
    }


    @JsonIgnore
    @Override
    public String getCode() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return null;
    }
}

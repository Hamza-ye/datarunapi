package org.nmcpye.platform.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nmcpye.platform.core.uid.UidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * A platform entity — anything the system tracks with stable identity.
 * Examples: facility, warehouse, item, user, org unit.
 *
 * Named "PlatformEntity" to avoid clash with javax.persistence.Entity annotation.
 * Database table: platform_entity
 */
@Getter
@Setter
@NoArgsConstructor
@jakarta.persistence.Entity
@Table(name = "platform_entity",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_entity_uid", columnNames = "uid")
       })
public class PlatformEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Stable, globally unique, immutable identifier.
     * Auto-generated if not provided.
     */
    @NotBlank
    @Size(max = 64)
    @Column(name = "uid", nullable = false, unique = true, updatable = false, length = 64)
    private String uid;

    /**
     * Human-readable code, unique within the entity type.
     */
    @Size(max = 100)
    @Column(name = "code", length = 100)
    private String code;

    @NotBlank
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_entity_type"))
    private EntityType type;

    /**
     * Typed attributes stored as JSONB. Schema optionally defined by EntityType.attributeSchema.
     */
    @Column(name = "attributes", columnDefinition = "jsonb")
    private String attributes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.uid == null || this.uid.isBlank()) {
            this.uid = UidGenerator.generate();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

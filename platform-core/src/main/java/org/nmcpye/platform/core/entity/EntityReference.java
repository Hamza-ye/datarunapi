package org.nmcpye.platform.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A typed, resolvable link between any platform construct and an entity.
 * Used to connect submissions, flow tasks, etc. to real-world entities.
 */
@Getter
@Setter
@NoArgsConstructor
@jakarta.persistence.Entity
@Table(name = "platform_entity_reference",
       indexes = {
           @Index(name = "idx_entity_ref_source", columnList = "source_type, source_id"),
           @Index(name = "idx_entity_ref_target", columnList = "target_entity_id")
       })
public class EntityReference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Type of the source construct (e.g., "submission", "flow_task").
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    /**
     * ID of the source construct.
     */
    @NotNull
    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    /**
     * The target entity being referenced.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_entity_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_entity_ref_target"))
    private PlatformEntity targetEntity;

    /**
     * Role of this reference (e.g., "target", "item_reference", "assigned_actor").
     */
    @NotBlank
    @Size(max = 50)
    @Column(name = "role", nullable = false, length = 50)
    private String role;
}

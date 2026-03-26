package org.nmcpye.platform.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Defines a type of entity in the platform (e.g., "facility", "item", "user").
 * Controls structural constraints and attribute schema.
 */
@Getter
@Setter
@NoArgsConstructor
@jakarta.persistence.Entity
@Table(name = "platform_entity_type",
       uniqueConstraints = @UniqueConstraint(name = "uk_entity_type_code", columnNames = "code"))
public class EntityType {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "structure", nullable = false, length = 20)
    private EntityStructure structure = EntityStructure.FLAT;

    /**
     * Optional JSON schema defining valid attribute keys and types
     * for entities of this type. Stored as JSONB in PostgreSQL.
     */
    @Column(name = "attribute_schema", columnDefinition = "jsonb")
    private String attributeSchema;
}

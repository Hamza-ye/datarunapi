package org.nmcpye.datarun.jpa.accessfilter.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nmcpye.datarun.sharedkernal.enumeration.AccessLevel;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * CQRS Materialized View representing a "flattened" resolution of a user's
 * access rights.
 * This table removes the need for API access filters to traverse the complex
 * Team->OrgUnit->Assignment tree at runtime.
 */
@Entity
@Table(name = "user_execution_context", indexes = {
        @Index(name = "idx_uec_user_entity", columnList = "user_uid, entity_type, entity_uid", unique = true),
        @Index(name = "idx_uec_entity", columnList = "entity_type, entity_uid")
})
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserExecutionContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uid", nullable = false, length = 11)
    private String userUid;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // e.g., 'ORG_UNIT', 'ACTIVITY', 'DATA_TEMPLATE', 'ASSIGNMENT'

    @Column(name = "entity_uid", nullable = false, length = 11)
    private String entityUid;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolved_permission", nullable = false, length = 20)
    private AccessLevel resolvedPermission; // 'READ', 'UPDATE', 'DELETE', 'ALL'

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private Instant createdDate;

    /// Provenance: track why a user has access
    /// e.g. `[{source:'ASSIGNMENT_ROLE_PARTY_POLICY', bindingId, principalType,
    /// principalId}, ...]`
    @org.hibernate.annotations.Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "provenance", columnDefinition = "jsonb")
    private java.util.Map<String, Object> provenance;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserExecutionContext that))
            return false;
        return Objects.equals(userUid, that.userUid) &&
                Objects.equals(entityType, that.entityType) &&
                Objects.equals(entityUid, that.entityUid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userUid, entityType, entityUid);
    }
}

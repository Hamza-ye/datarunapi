package org.nmcpye.datarun.party.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;
import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

/// @author Hamza Assada 28/12/2025
@Entity
@Table(name = "party_set")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartySet extends JpaIdentifiableObject implements TranslatableInterface {

    @Data
    @Builder
    public static class PartySetSpec {
        private UUID rootId; // For ORG_TREE
        private Integer depth; // nullable For ORG_TREE
        private Boolean includeSelf; // For ORG_TREE

        private List<String> tags; // For TAG_FILTER
        private List<String> types; // ORG_UNIT, TEAM, USER. Nullable For Specific TAG_FILTER

        private String sqlKey; // For QUERY
        private Map<String, Object> params; // For QUERY
    }

    @Column(name = "uid", length = 11, updatable = false, unique = true, nullable = false)
    protected String uid;

    @Column(name = "code", unique = true, length = 32)
    protected String code;

    @Column(name = "name", nullable = false)
    protected String name;

    @Column(length = 32, nullable = false)
    @Enumerated(EnumType.STRING)
    private PartySetKind kind;

    @Column(columnDefinition = "jsonb default '{}'::jsonb", nullable = false)
    @Type(JsonType.class)
    private PartySetSpec spec;

    @Column(name = "is_materialized")
    @Builder.Default
    protected Boolean isMaterialized = Boolean.TRUE;

    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    @Builder.Default
    protected Set<Translation> translations = new HashSet<>();
}

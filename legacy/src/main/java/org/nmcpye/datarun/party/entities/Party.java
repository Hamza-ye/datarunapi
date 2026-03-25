package org.nmcpye.datarun.party.entities;

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

import java.util.HashSet;
import java.util.Set;

/// @author Hamza Assada 28/12/2025
@Entity
@Table(name = "party")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Party extends JpaIdentifiableObject implements TranslatableInterface {
    public enum PartyType {
        INTERNAL, EXTERNAL
    }

    public enum SourceType {
        ORG_UNIT, ACTIVITY, TEAM, USER, STATIC, EXTERNAL
    }

    @Column(name = "uid", length = 11, unique = true, nullable = false, updatable = false)
    protected String uid;

    @Column(name = "code", length = 32)
    protected String code;

    @Column(name = "name", nullable = false)
    protected String name;

    /// INTERNAL, EXTERNAL
    @Column(name = "type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private PartyType type;

    /// ORG_UNIT, TEAM, USER, STATIC, EXTERNAL Types
    @Column(name = "source_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Column(name = "source_id", length = 64, nullable = false)
    private String sourceId;

    /// for parties with parents such as orgUnits,
    /// we use `orgUnit.id` which is the same as orgUnit's `party.source_id` of the
    /// parent org_unit
    @Column(name = "parent_id", length = 26)
    private String parentId;

    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    @Builder.Default
    protected Set<Translation> translations = new HashSet<>();
}

package org.nmcpye.datarun.jpa.orgunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A OrgUnitGroup.
 */
@Entity
@Table(name = "org_unit_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrgUnitGroup extends JpaIdentifiableObject implements TranslatableInterface {
    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", unique = true)
    protected String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", nullable = false, unique = true)
    protected String name;

    @Column(name = "symbol")
    private String symbol;

    @Column(name = "color")
    private String color;

    @Column(name = "inactive")
    private Boolean inactive = false;

    @Type(JsonType.class)
    @Column(name = "properties_map", columnDefinition = "jsonb")
    @JsonProperty
    protected Map<String, Object> properties;

    @ManyToMany
    @JoinTable(name = "org_unit_group_members", joinColumns = @JoinColumn(name = "group_id"), inverseJoinColumns = @JoinColumn(name = "org_unit_id"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "parent", "children", "orgUnitGroups", "assignments", "hierarchyLevel", "ancestors",
            "translations", "path" }, allowSetters = true)
    private Set<OrgUnit> orgUnits = new HashSet<>();

    @ManyToMany(mappedBy = "orgUnitGroups")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "orgUnitGroups", "translations" }, allowSetters = true)
    private Set<OrgUnitGroupSet> orgUnitGroupSets = new HashSet<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    @JsonProperty
    public String getColor() {
        return this.color;
    }

    @JsonProperty
    public Boolean getInactive() {
        return this.inactive;
    }
}

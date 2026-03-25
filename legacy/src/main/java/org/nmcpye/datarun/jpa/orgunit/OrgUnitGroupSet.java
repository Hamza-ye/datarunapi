package org.nmcpye.datarun.jpa.orgunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import java.util.Set;

/**
 * A OrgUnitGroupSet.
 */
@Entity
@Table(name = "org_unit_groupset")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings({ "common-java:DuplicatedBlocks", "unused" })
public class OrgUnitGroupSet extends JpaIdentifiableObject implements TranslatableInterface {
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

    @ManyToMany
    @JoinTable(name = "org_unit_groupset_org_unit_group", joinColumns = @JoinColumn(name = "groupset_id"), inverseJoinColumns = @JoinColumn(name = "org_unit_group_id"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "orgUnitGroupSets", "orgUnits" }, allowSetters = true)
    private Set<OrgUnitGroup> orgUnitGroups = new HashSet<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    public boolean hasOrgUnitGroups() {
        return orgUnitGroups != null && !orgUnitGroups.isEmpty();
    }

    public boolean isMemberOfOrgUnitGroups(OrgUnit organisationUnit) {
        for (OrgUnitGroup group : orgUnitGroups) {
            if (group.getOrgUnits().contains(organisationUnit)) {
                return true;
            }
        }

        return false;
    }

    public OrgUnitGroupSet addOrgUnitGroup(OrgUnitGroup orgUnitGroup) {
        this.orgUnitGroups.add(orgUnitGroup);
        orgUnitGroup.getOrgUnitGroupSets().add(this);
        return this;
    }

    public OrgUnitGroupSet removeOrgUnitGroup(OrgUnitGroup orgUnitGroup) {
        this.orgUnitGroups.remove(orgUnitGroup);
        orgUnitGroup.getOrgUnitGroupSets().remove(this);
        return this;
    }
}

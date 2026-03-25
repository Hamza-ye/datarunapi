package org.nmcpye.datarun.jpa.orgunit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.sharedkernal.IdentifiableObjectUtils;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.*;

/**
 * A OrgUnit.
 *
 * @author Hamza Assada 18/01/2022
 */
@Entity
@Table(name = "org_unit")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings({ "common-java:DuplicatedBlocks", "unused" })
public class OrgUnit extends JpaIdentifiableObject implements TranslatableInterface {

    private static final String PATH_SEP = ",";

    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", nullable = false, unique = true)
    protected String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", nullable = false)
    protected String name;

    @Column(name = "path")
    private String path;

    @JsonIgnore
    @Column(name = "level")
    private Integer hierarchyLevel;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "orgUnit")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonSerialize(contentAs = JpaIdentifiableObject.class)
    @JsonIgnoreProperties(value = { "activity", "team", "orgUnit", "parent", "children", "ancestors", "level",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    private Set<Assignment> assignments = new HashSet<>();

    @ManyToOne // (fetch = FetchType.LAZY)
    @JsonProperty
    @JsonIgnoreProperties(value = { "parent", "children", "orgUnitGroups", "assignments", "hierarchyLevel", "ancestors",
            "translations" }, allowSetters = true)
    private OrgUnit parent;

    @ManyToMany(mappedBy = "orgUnits")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "orgUnitGroupSets", "orgUnits", "translations" }, allowSetters = true)
    private Set<OrgUnitGroup> orgUnitGroups = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "parent", "children", "orgUnitGroups", "assignments", "hierarchyLevel", "ancestors",
            "translations" }, allowSetters = true)
    private Set<OrgUnit> children = new HashSet<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "properties_map", columnDefinition = "jsonb")
    @JsonProperty
    protected Map<String, Object> properties;

    @PreRemove
    private void removeOuGroupsFromOu() {
        for (OrgUnitGroup g : orgUnitGroups) {
            g.getOrgUnits().remove(this);
        }
        if (parent != null) {
            parent.getChildren().remove(this);
        }
    }

    public void removeOrganisationUnitGroup(OrgUnitGroup organisationUnitGroup) {
        orgUnitGroups.remove(organisationUnitGroup);
        organisationUnitGroup.getOrgUnits().remove(this);
    }

    public void removeAllOrganisationUnitGroups() {
        for (OrgUnitGroup organisationUnitGroup : orgUnitGroups) {
            organisationUnitGroup.getOrgUnits().remove(this);
        }

        orgUnitGroups.clear();
    }

    public void updateParent(OrgUnit newParent) {
        if (this.parent != null && this.parent.getChildren() != null) {
            this.parent.getChildren().remove(this);
        }

        this.parent = newParent;

        newParent.getChildren().add(this);
    }

    public OrgUnit children(Set<OrgUnit> organisationUnits) {
        this.setChildren(organisationUnits);
        return this;
    }

    public OrgUnit addChildren(OrgUnit organisationUnit) {
        this.children.add(organisationUnit);
        organisationUnit.setParent(this);
        return this;
    }

    public OrgUnit removeChildren(OrgUnit organisationUnit) {
        this.children.remove(organisationUnit);
        organisationUnit.setParent(null);
        return this;
    }

    public OrgUnit addGroup(OrgUnitGroup organisationUnitGroup) {
        this.orgUnitGroups.add(organisationUnitGroup);
        organisationUnitGroup.getOrgUnits().add(this);
        return this;
    }

    public OrgUnit removeGroup(OrgUnitGroup organisationUnitGroup) {
        this.orgUnitGroups.remove(organisationUnitGroup);
        organisationUnitGroup.getOrgUnits().remove(this);
        return this;
    }

    public String getPath() {
        List<String> pathList = new ArrayList<>();
        Set<String> visitedSet = new HashSet<>();
        OrgUnit unit = parent;

        pathList.add(getUid());

        while (unit != null) {
            if (!visitedSet.contains(unit.getUid())) {
                pathList.add(unit.getUid());
                visitedSet.add(unit.getUid());
                unit = unit.getParent();
            } else {
                unit = null; // Protect against cyclic org unit graphs
            }
        }

        Collections.reverse(pathList);

        this.path = PATH_SEP + StringUtils.join(pathList, PATH_SEP);

        return this.path;
    }

    /**
     * Returns the list of ancestor organisation unit UIDs up to any of the given
     * roots for this organisation unit. Does not include itself. The list is
     * ordered by root first.
     *
     * @param rootUids the root organisation units, if null using real roots.
     */
    public List<String> getAncestorUids(Set<String> rootUids) {
        if (path == null || path.isEmpty()) {
            return Lists.newArrayList();
        }

        String[] ancestors = path.substring(1).split(PATH_SEP); // Skip first delimiter, root unit first
        int lastIndex = ancestors.length - 2; // Skip this unit
        List<String> uids = Lists.newArrayList();

        for (int i = lastIndex; i >= 0; i--) {
            String uid = ancestors[i];
            uids.add(0, uid);

            if (rootUids != null && rootUids.contains(uid)) {
                break;
            }
        }

        return uids;
    }

    /**
     * Returns a string representing the graph of ancestors. The string is delimited
     * by "/". The ancestors are ordered by root first and represented by UIDs.
     *
     * @param roots the root organisation units, if null using real roots.
     */
    public String getParentGraph(Collection<OrgUnit> roots) {
        Set<String> rootUids = roots != null ? Sets.newHashSet(String.valueOf(IdentifiableObjectUtils.getUids(roots)))
                : null;
        List<String> ancestors = getAncestorUids(rootUids);
        return StringUtils.join(ancestors, PATH_SEP);
    }

    /**
     * Returns a string representing the graph of ancestors. The string is delimited
     * by ",". The ancestors are ordered by root first and represented by names.
     *
     * @param roots       the root organisation units, if null using real roots.
     * @param includeThis whether to include this organisation unit in the graph.
     */
    public String getParentNameGraph(Collection<OrgUnit> roots, boolean includeThis) {
        StringBuilder builder = new StringBuilder();

        List<OrgUnit> ancestors = getAncestors(roots);

        for (OrgUnit unit : ancestors) {
            builder.append(PATH_SEP).append(unit.getName());
        }

        if (includeThis) {
            builder.append(PATH_SEP).append(name);
        }

        return builder.toString();
    }

    /**
     * Returns a mapping between the id and the id parent graph of the given
     * organisation units.
     */
    public static Map<String, String> getParentGraphMap(List<OrgUnit> organisationUnits, Collection<OrgUnit> roots) {
        Map<String, String> map = new HashMap<>();

        if (organisationUnits != null) {
            for (OrgUnit unit : organisationUnits) {
                map.put(unit.getUid(), unit.getParentGraph(roots));
            }
        }

        return map;
    }

    // @JsonProperty(value = "level", access = JsonProperty.Access.READ_ONLY)
    public Integer getLevel() {
        return StringUtils.countMatches(path, PATH_SEP);
    }

    // for Hibernate
    public void setLevel(Integer ouLevel) {
        // this.level = ouLevel;
    }

    /**
     * Used by persistence layer. Purpose is to have a column for use in database
     * queries. For application use see {@link OrgUnit#getLevel()} which
     * has better performance.
     */
    public Integer getHierarchyLevel() {
        Set<String> uids = Sets.newHashSet(getUid());

        OrgUnit current = this;

        while ((current = current.getParent()) != null) {
            boolean add = uids.add(current.getUid());

            if (!add) {
                break; // Protect against cyclic org unit graphs
            }
        }

        hierarchyLevel = uids.size();

        return hierarchyLevel;
    }

    /**
     * Returns the list of ancestor organisation units for this organisation unit.
     * Does not include itself. The list is ordered by root first.
     *
     * @throws IllegalStateException if circular parent relationships is detected.
     */
    @JsonIgnoreProperties(value = { "parent", "children", "ancestors", "orgUnitGroups", "assignments", "createdBy",
            "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    public List<OrgUnit> getAncestors() {
        List<OrgUnit> units = new ArrayList<>();
        Set<OrgUnit> visitedUnits = new HashSet<>();

        OrgUnit unit = parent;

        while (unit != null) {
            if (!visitedUnits.add(unit)) {
                throw new IllegalStateException(
                        "Organisation unit '" + this + "' has circular parent relationships: '" + unit + "'");
            }

            units.add(unit);
            unit = unit.getParent();
        }

        Collections.reverse(units);
        return units;
    }

    /**
     * Returns the list of ancestor organisation units up to any of the given roots
     * for this organisation unit. Does not include itself. The list is ordered by
     * root first.
     *
     * @param roots the root organisation units, if null using real roots.
     */
    public List<OrgUnit> getAncestors(Collection<OrgUnit> roots) {
        List<OrgUnit> units = new ArrayList<>();
        OrgUnit unit = parent;

        while (unit != null) {
            units.add(unit);

            if (roots != null && roots.contains(unit)) {
                break;
            }

            unit = unit.getParent();
        }

        Collections.reverse(units);
        return units;
    }

    public OrgUnit parent(OrgUnit parent) {
        this.setParent(parent);
        return this;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "OrgUnit{" +
                "id=" + getId() +
                ", id='" + getUid() + "'" +
                ", code='" + getCode() + "'" +
                ", name='" + getName() + "'" +
                ", ouPath='" + getPath() + "'" +
                "}";
    }
}

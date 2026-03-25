package org.nmcpye.datarun.assignment.assignment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.jpa.orgunit.OrgUnit;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.nmcpye.datarun.party.entities.AssignmentRoleDataPolicy;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;
import org.nmcpye.datarun.party.entities.PartySet;
import org.nmcpye.datarun.sharedkernal.IdentifiableObjectUtils;
import org.nmcpye.datarun.sharedkernal.JpaSoftDeleteObject;
import org.nmcpye.datarun.sharedkernal.TranslatableInterface;
import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A Assignment.
 */
@Entity
@Table(name = "assignment", uniqueConstraints = {
        @UniqueConstraint(name = "uc_assignment_activity_id", columnNames = { "activity_id", "org_unit_id", "team_id" })
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@JsonIgnoreProperties(value = { "new" })
@Getter
@Setter
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Assignment extends JpaSoftDeleteObject implements TranslatableInterface {

    private static final String PATH_SEP = ",";

    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @NotNull
    @ManyToOne // (fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "project", "translations", "assignments" }, allowSetters = true)
    private Activity activity;

    @Column(name = "start_day")
    private Integer startDay;

    @ManyToOne // (fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "parent", "children", "orgUnitGroups", "assignments",
            "hierarchyLevel", "ancestors", "translations" }, allowSetters = true)
    private OrgUnit orgUnit;

    @ManyToOne
    @JsonIgnoreProperties(value = { "managedTeams", "managedByTeams", "users", "assignments",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy", "activity", "teamFormAccesses",
            "formPermissions" }, allowSetters = true)
    private Team team;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "activity", "team", "orgUnit", "parent", "children", "ancestors", "level",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    private Set<Assignment> children = new HashSet<>();

    @Column(name = "path")
    private String path;

    @JsonIgnore
    @Column(name = "level")
    private Integer hierarchyLevel;

    @ManyToOne
    @JsonProperty
    @JsonIgnoreProperties(value = { "defaultPartySet", "activity", "team", "orgUnit", "parent", "children", "ancestors",
            "level", "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    private Assignment parent;

    @Type(JsonType.class)
    @Column(name = "forms", columnDefinition = "jsonb")
    private Set<String> forms = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "allocated_resources", columnDefinition = "jsonb")
    private Map<String, Object> allocatedResources = new HashMap<>();

    // @OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment")
    // @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    // @JsonIgnoreProperties(value = {"assignment"}, allowSetters = true)
    // private Set<AssignmentHistory> assignmentHistory = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private FlowStatus status;

    @Column(name = "last_submitted_by")
    private String lastSubmittedBy;

    @Type(JsonType.class)
    @Column(name = "properties_map", columnDefinition = "jsonb")
    @JsonProperty
    protected Map<String, Object> properties;

    @ManyToOne
    @JsonIgnoreProperties(value = { "createdBy", "createdDate", "lastModifiedDate",
            "lastModifiedBy" }, allowSetters = true)
    @JoinColumn(name = "default_party_set_id")
    private PartySet defaultPartySet;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name")
    protected String name;

    @Column(name = "code", length = 100, unique = true)
    protected String code;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<AssignmentRoleDataPolicy> dataTemplates = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "assignment")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<AssignmentRolePartyPolicy> bindings = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<AssignmentMember> members = new HashSet<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    @JsonProperty(value = "progressStatus")
    public FlowStatus getStatus() {
        return status;
    }

    @JsonProperty
    public Map<String, Object> getAllocatedResources() {
        return allocatedResources;
    }

    /**
     * Returns the list of ancestor assignment UIDs up to any of the given
     * roots for this assignment. Does not include itself. The list is
     * ordered by root first.
     *
     * @param rootUids the root activities, if null using real roots.
     */
    public List<String> getAncestorUids(Set<String> rootUids) {
        if (path == null || path.isEmpty()) {
            return Lists.newArrayList();
        }

        String[] ancestors = path.substring(1).split(PATH_SEP); // Skip first delimiter, root assignment first
        int lastIndex = ancestors.length - 2; // Skip this assignment
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
     * @param roots the root activities, if null using real roots.
     */
    public String getParentGraph(Collection<Assignment> roots) {
        Set<String> rootUids = roots != null ? Sets.newHashSet(String.valueOf(IdentifiableObjectUtils.getUids(roots)))
                : null;
        List<String> ancestors = getAncestorUids(rootUids);
        return StringUtils.join(ancestors, PATH_SEP);
    }

    /**
     * Returns a mapping between the uid and the uid parent graph of the given
     * activities.
     */
    public static Map<String, String> getParentGraphMap(List<Assignment> activities, Collection<Assignment> roots) {
        Map<String, String> map = new HashMap<>();

        if (activities != null) {
            for (Assignment assignment : activities) {
                map.put(assignment.getUid(), assignment.getParentGraph(roots));
            }
        }

        return map;
    }

    @JsonProperty(value = "level", access = JsonProperty.Access.READ_ONLY)
    public Integer getLevel() {
        return StringUtils.countMatches(path, PATH_SEP);
    }

    // for Hibernate
    public void setLevel(Integer ouLevel) {
        // this.level = ouLevel;
    }

    /**
     * Used by persistence layer. Purpose is to have a column for use in database
     * queries. For application use see {@link Assignment#getLevel()} which
     * has better performance.
     */
    public Integer getHierarchyLevel() {
        Set<String> uids = Sets.newHashSet(getUid());

        Assignment current = this;

        while ((current = current.getParent()) != null) {
            boolean add = uids.add(current.getUid());

            if (!add) {
                break; // Protect against cyclic org assignment graphs
            }
        }

        hierarchyLevel = uids.size();

        return hierarchyLevel;
    }

    public Assignment hierarchyLevel(Integer hierarchyLevel) {
        this.setHierarchyLevel(hierarchyLevel);
        return this;
    }

    /**
     * Returns the list of ancestor activities for this assignment.
     * Does not include itself. The list is ordered by root first.
     *
     * @throws IllegalStateException if circular parent relationships is detected.
     */
    @JsonIgnore
    // @JsonProperty("ancestors")
    // @JsonSerialize(contentAs = Identifiable.class)
    public List<Assignment> getAncestors() {
        List<Assignment> activities = new ArrayList<>();
        Set<Assignment> visitedActivities = new HashSet<>();

        Assignment assignment = parent;

        while (assignment != null) {
            if (!visitedActivities.add(assignment)) {
                throw new IllegalStateException(
                        "Assignment '" + this.toString() + "' has circular parent relationships: '" + assignment + "'");
            }

            activities.add(assignment);
            assignment = assignment.getParent();
        }

        Collections.reverse(activities);
        return activities;
    }

    /**
     * Returns the list of ancestors up to any of the given roots
     * for this assignment. Does not include itself. The list is ordered by
     * root first.
     *
     * @param roots the root activities, if null using real roots.
     */
    public List<Assignment> getAncestors(Collection<Assignment> roots) {
        List<Assignment> assignments = new ArrayList<>();
        Assignment assignment = parent;

        while (assignment != null) {
            assignments.add(assignment);

            if (roots != null && roots.contains(assignment)) {
                break;
            }

            assignment = assignment.getParent();
        }

        Collections.reverse(assignments);
        return assignments;
    }

    // public String getPath() {
    // return path;
    // }
    public String getPath() {
        List<String> pathList = new ArrayList<>();
        Set<String> visitedSet = new HashSet<>();
        Assignment assignment = parent;

        pathList.add(getUid());

        while (assignment != null) {
            if (!visitedSet.contains(assignment.getUid())) {
                pathList.add(assignment.getUid());
                visitedSet.add(assignment.getUid());
                assignment = assignment.getParent();
            } else {
                assignment = null; // Protect against cyclic org unit graphs
            }
        }

        Collections.reverse(pathList);

        this.path = PATH_SEP + StringUtils.join(pathList, PATH_SEP);

        return this.path;
    }

    public boolean isDescendant(Assignment ancestor) {
        if (ancestor == null) {
            return false;
        }

        Assignment unit = this;

        while (unit != null) {
            if (ancestor.equals(unit)) {
                return true;
            }

            unit = unit.getParent();
        }

        return false;
    }

    @JsonIgnore
    public boolean isDescendant(Set<Assignment> ancestors) {
        if (ancestors == null || ancestors.isEmpty()) {
            return false;
        }
        Set<String> ancestorsUid = ancestors.stream().map(Assignment::getUid).collect(Collectors.toSet());

        Assignment unit = this;

        while (unit != null) {
            if (ancestorsUid.contains(unit.getUid())) {
                return true;
            }

            unit = unit.getParent();
        }

        return false;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "id=" + getId() +
                ", uid='" + getUid() + "'" +
                ", createdBy='" + getCreatedBy() + "'" +
                ", createdDate='" + getCreatedDate() + "'" +
                ", lastModifiedBy='" + getLastModifiedBy() + "'" +
                ", lastModifiedDate='" + getLastModifiedDate() + "'" +
                "}";
    }
}

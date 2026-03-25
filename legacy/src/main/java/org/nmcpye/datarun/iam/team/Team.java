package org.nmcpye.datarun.iam.team;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.enumeration.FormPermission;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A Team.
 *
 * @author Hamza Assada
 * @since 02/06/2023
 */
@Entity
@Table(name = "team", uniqueConstraints = {
        @UniqueConstraint(name = "uc_team_code_activity_id", columnNames = { "code", "activity_id" })
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings({ "common-java:DuplicatedBlocks", "unused" })
public class Team extends JpaIdentifiableObject {

    @Size(max = 11)
    @Column(name = "uid", length = 11, updatable = false, unique = true)
    protected String uid;

    /**
     * The unique code for this object.
     */
    @Column(name = "code", nullable = false)
    protected String code;

    /**
     * The name of this object. Required and unique.
     */
    @Column(name = "name", nullable = false)
    protected String name;

    @Column(name = "description")
    private String description;

    @Column(name = "disabled")
    private Boolean disabled = false;

    @ManyToOne
    @JsonIgnoreProperties(value = { "project", "translations", "assignments" }, allowSetters = true)
    @JsonSerialize(contentAs = IdentifiableObject.class)
    private Activity activity;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "team_user", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties(value = { "teams", "password", "authorities", "userGroups", "managedGroups",
            "managedByGroups" }, allowSetters = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<User> users = new HashSet<>();

    /// //
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "team_managed_teams", joinColumns = @JoinColumn(name = "team_id"), inverseJoinColumns = @JoinColumn(name = "managed_team_id"))
    @JsonIgnoreProperties(value = { "managedTeams", "managedByTeams", "users", "assignments",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy", "activity", "teamFormAccesses",
            "formPermissions" }, allowSetters = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Team> managedTeams = new HashSet<>();

    @ManyToMany(mappedBy = "managedTeams")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "managedTeams", "managedByTeams", "users", "assignments",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy", "activity", "teamFormAccesses",
            "formPermissions" }, allowSetters = true)
    private Set<Team> managedByTeams = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "team")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "activity", "team", "orgUnit", "parent", "children", "ancestors", "level",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    private Set<Assignment> assignments = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "form_permissions", columnDefinition = "jsonb")
    private Set<TeamFormPermissions> formPermissions = new HashSet<>();

    @Type(JsonType.class)
    @Column(name = "properties_map", columnDefinition = "jsonb")
    @JsonProperty
    protected Map<String, Object> properties;

    public Set<FormPermission> getFormPermissions(String form) {
        return formPermissions.stream()
                .filter(p -> p.getForm().equals(form))
                .flatMap(p -> p.getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public Set<String> getFormWithPermission(FormPermission permission) {
        return formPermissions.stream()
                .filter(p -> p.getPermissions().contains(permission))
                .map(p -> p.getForm())
                .collect(Collectors.toSet());
    }

    public Set<String> getFormWithAnyPermission(List<FormPermission> permissionList) {
        return formPermissions.stream()
                .filter(fp -> fp.getPermissions().stream().anyMatch(permissionList::contains))
                .map(fp -> fp.getForm())
                .collect(Collectors.toSet());
    }

    public boolean hasFormPermission(String formId, FormPermission permission) {
        var permissions = this.formPermissions.stream()
                .filter(teamFormPermissions -> Objects.equals(teamFormPermissions.getForm(), formId)).findFirst();
        return permissions.isPresent() && permissions.get().getPermissions().contains(permission);
    }

    public boolean hasAnyOfFormPermission(String formId, List<FormPermission> permissionsList) {
        var permissions = this.formPermissions.stream()
                .filter(teamFormPermissions -> Objects.equals(teamFormPermissions.getForm(), formId)).findFirst();
        return permissions.isPresent() && permissions.get().getPermissions()
                .stream().anyMatch(permissionsList::contains);
    }

    public Set<String> getFormsWithPermission(FormPermission permission) {
        // for old saved and not migrated teams, who have formPermissions == null
        if (this.formPermissions != null) {
            return this.formPermissions.stream()
                    .filter(entry -> entry.getPermissions().contains(permission))
                    .map(entry -> entry.getForm())
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public void setAssignments(Set<Assignment> flowInstances) {
        if (this.assignments != null) {
            this.assignments.forEach(i -> i.setTeam(null));
        }
        if (flowInstances != null) {
            flowInstances.forEach(i -> i.setTeam(this));
        }
        this.assignments = flowInstances;
    }

    public Team assignments(Set<Assignment> flowInstances) {
        this.setAssignments(flowInstances);
        return this;
    }
}

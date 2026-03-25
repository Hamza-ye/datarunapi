package org.nmcpye.datarun.iam.usegroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.translation.Translation;

import java.util.HashSet;
import java.util.Set;

/**
 * A UserGroup.
 */
@Entity
@Table(name = "user_group")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@SuppressWarnings({ "common-java:DuplicatedBlocks", "unused", "UnusedReturnValue" })
public class UserGroup extends JpaIdentifiableObject {

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

    @Column(name = "description")
    private String description;

    @Column(name = "disabled")
    private Boolean disabled = false;

    @Transient
    private boolean isPersisted;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_group_users", joinColumns = @JoinColumn(name = "user_group_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    @JsonIgnoreProperties(value = { "teams", "password", "authorities", "userGroups", "managedGroups",
            "managedByGroups" }, allowSetters = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<User> users = new HashSet<>();

    /// //
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_group_managed_groups", joinColumns = @JoinColumn(name = "user_group_id"), inverseJoinColumns = @JoinColumn(name = "managed_group_id"))
    @JsonIgnoreProperties(value = { "managedGroups", "managedByGroups", "users",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserGroup> managedGroups = new HashSet<>();

    @ManyToMany(mappedBy = "managedGroups")
    @JsonIgnoreProperties(value = { "managedGroups", "managedByGroups", "users",
            "createdBy", "createdDate", "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserGroup> managedByGroups = new HashSet<>();

    /**
     * Set of available object translation, normally filtered by locale.
     */
    @Type(JsonType.class)
    @Column(name = "translations", columnDefinition = "jsonb")
    protected Set<Translation> translations = new HashSet<>();

    public UserGroup addMember(User user) {
        this.users.add(user);
        user.getUserGroups().add(this);
        return this;
    }

    public Set<UserGroup> getManagedGroups() {
        return this.managedGroups;
    }

    public Set<UserGroup> getManagedByGroups() {
        return this.managedByGroups;
    }

    public UserGroup removeMember(User user) {
        this.users.remove(user);
        user.getUserGroups().remove(this);
        return this;
    }

    public UserGroup addManagedGroup(UserGroup userGroup) {
        this.managedGroups.add(userGroup);
        userGroup.getManagedByGroups().add(this);
        return this;
    }

    public UserGroup removeManagedGroup(UserGroup userGroup) {
        this.managedGroups.remove(userGroup);
        userGroup.getManagedByGroups().remove(this);
        return this;
    }

    public UserGroup addManagedByGroup(UserGroup userGroup) {
        this.managedByGroups.add(userGroup);
        userGroup.getManagedGroups().add(this);
        return this;
    }

    public UserGroup removeManagedByGroup(UserGroup userGroup) {
        this.managedByGroups.remove(userGroup);
        userGroup.getManagedGroups().remove(this);
        return this;
    }
}

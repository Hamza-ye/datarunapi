package org.nmcpye.datarun.iam.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CurrentUserDetails extends UserDetails {
    @Override
    Collection<? extends GrantedAuthority> getAuthorities();

    @JsonIgnore
    @Override
    String getPassword();

    @Override
    String getUsername();

    @JsonIgnore
    @Override
    boolean isAccountNonExpired();

    @JsonIgnore
    @Override
    boolean isAccountNonLocked();

    @JsonIgnore
    @Override
    boolean isCredentialsNonExpired();

    @JsonProperty(value = "activated")
    @Override
    boolean isEnabled();

    String getId();
    /// Data run user's attributes
    @JsonProperty(value = "id")
    String getUid();

    String getMobile();

    String getFirstName();

    String getLastName();

    String getEmail();

    String getImageUrl();

    String getLangKey();

    @JsonIgnore
    boolean isSuper();

    // metadata
    Integer getAssignmentCount();

    Integer getOrgUnitCount();

    /**
     * Set of UserTeam UID which current User belongs to.
     *
     * @return user Teams uids
     */
    Set<String> getUserTeamsIds();
    Set<String> getUserTeamsUIDs();

    Set<String> getActivityUIDs();

    Set<String> getManagedTeamsIds();
    Set<String> getManagedTeamsUIDs();

    Set<String> getUserGroupsIds();
    Set<String> getUserGroupsUIDs();

    Set<String> getUserFormsUIDs();

    List<UserFormAccess> getFormAccess();
}

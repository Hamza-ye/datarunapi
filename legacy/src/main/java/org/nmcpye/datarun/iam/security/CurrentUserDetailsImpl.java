package org.nmcpye.datarun.iam.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
@AllArgsConstructor
@Getter
@Builder
public class CurrentUserDetailsImpl implements CurrentUserDetails {
    private final String id;
    private final String uid;

    private final String username;

    @JsonIgnore
    private final String password;

    private final boolean enabled;

    private final boolean accountNonExpired;

    private final boolean accountNonLocked;

    private final boolean credentialsNonExpired;

    private final Collection<GrantedAuthority> authorities;

    private final String mobile;
    private final String email;
    private final String imageUrl;
    private final String firstName;
    private final String lastName;
    private final String langKey;
    private final boolean isSuper;

    private final Integer assignmentCount;
    private final Integer orgUnitCount;
    private final Set<String> activityUIDs;

    private final Set<String> userTeamsIds;
    private final Set<String> userTeamsUIDs;

    private final Set<String> managedTeamsUIDs;
    private final Set<String> managedTeamsIds;

    private final Set<String> userGroupsUIDs;
    private final Set<String> userGroupsIds;

    private final Set<String> userFormsUIDs;
    private final Set<String> userActiveFlowIds;
    private final List<UserFormAccess> formAccess;
}

package org.nmcpye.datarun.iam.security;

import org.nmcpye.datarun.iam.user.User;
import org.nmcpye.datarun.iam.user.UserNotActivatedException;
import org.nmcpye.datarun.iam.userauthority.Authority;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CreatUserDetailService {
    final private CurrentUserInfoService currentUserInfoService;

    public CreatUserDetailService(CurrentUserInfoService currentUserInfoService) {
        this.currentUserInfoService = currentUserInfoService;
    }


    /// //////
    public CurrentUserDetails createUserDetails(User user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + user.getLogin().toLowerCase() + " was not activated");
        }

        final var userTeamInfo = currentUserInfoService
            .getUserTeamInfo(user.getLogin());
        final var userGroups = currentUserInfoService.getUserGroupIds(user.getLogin());

        final var userFormAccess = currentUserInfoService
            .getUserFormAccess
                (user.getLogin(), userTeamInfo.getTeamUIDs());

        return CurrentUserDetailsImpl.builder()
            .id(user.getId())
            .uid(user.getUid())
            .username(user.getLogin())
            .password(user.getPassword())
            .enabled(user.isActivated())
            .accountNonExpired(user.isActivated())
            .accountNonLocked(user.isActivated())
            .credentialsNonExpired(user.isActivated())
            // TODO: migrate to use User's roles
            .authorities(user.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                .collect(Collectors.toList()))

            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .mobile(user.getMobile())
            .langKey(user.getLangKey())
            .imageUrl(user.getImageUrl())

            .isSuper(user.getAuthorities()
                .stream()
                .map(Authority::getName)
                .anyMatch((s) ->
                    s.equals(AuthoritiesConstants.ADMIN)))

            .userTeamsIds(userTeamInfo.getTeamIds())
            .userTeamsUIDs(userTeamInfo.getTeamUIDs())

            .managedTeamsIds(userTeamInfo.getManagedTeamIds())
            .managedTeamsUIDs(userTeamInfo.getManagedTeamUIDs())

            .activityUIDs(currentUserInfoService
                .getUserActivityInfo(user.getLogin()).getActivityUIDs())


            .userGroupsIds(userGroups.getUserGroupIds())
            .userGroupsUIDs(userGroups.getUserGroupUIDs())

            .userFormsUIDs(userFormAccess.stream()
                .map(UserFormAccess::getForm)
                .collect(Collectors.toSet()))
            .userActiveFlowIds(Collections.emptySet())
            .formAccess(userFormAccess)

            .build();
    }
}

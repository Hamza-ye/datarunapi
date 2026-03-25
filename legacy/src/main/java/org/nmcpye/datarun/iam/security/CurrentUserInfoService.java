package org.nmcpye.datarun.iam.security;

import org.nmcpye.datarun.assignment.activity.Activity;
import org.nmcpye.datarun.iam.team.Team;
import org.nmcpye.datarun.iam.team.repository.TeamRepository;
import org.nmcpye.datarun.iam.team.repository.TeamSpecifications;
import org.nmcpye.datarun.iam.usegroup.UserGroup;
import org.nmcpye.datarun.iam.usegroup.repository.UserGroupRepository;
import org.nmcpye.datarun.iam.user.repository.UserRepository;
import org.nmcpye.datarun.iam.userdetail.CurrentUserActivityInfo;
import org.nmcpye.datarun.iam.userdetail.CurrentUserGroupInfo;
import org.nmcpye.datarun.iam.userdetail.CurrentUserTeamInfo;
import org.nmcpye.datarun.iam.userdetail.UserFormAccess;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CurrentUserInfoService {
    final private TeamRepository teamRepository;
    final private UserRepository userRepository;
    final private UserGroupRepository userGroupRepository;

    public CurrentUserInfoService(TeamRepository teamRepository, UserRepository userRepository,
                                  UserGroupRepository userGroupRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.userGroupRepository = userGroupRepository;
    }

    @Cacheable(cacheNames = UserRepository.USER_TEAM_IDS_CACHE, key = "#userLogin")
    public CurrentUserTeamInfo getUserTeamInfo(String userLogin) {
        final var user = userRepository.findOneWithAuthoritiesByLogin(userLogin).orElseThrow(() ->
            new UsernameNotFoundException("User with login " + userLogin + " was not found in the database"));
        final var teams = new HashSet<>(teamRepository.findAllByUserLogin(userLogin, false));
        final var managedTeams = teams.stream()
            .flatMap(team -> team.getManagedTeams().stream())
            .filter(team -> !team.getDisabled())
            .filter(team -> !team.getActivity().getDisabled())
            .toList();

        final var teamUids = teams
            .stream().map(Team::getUid)
            .collect(Collectors.toSet());
        final var teamIds = teams
            .stream().map(Team::getId)
            .collect(Collectors.toSet());

        final var managedTeamUids = managedTeams
            .stream().map(Team::getUid)
            .collect(Collectors.toSet());
        final var managedTeamIds = managedTeams
            .stream().map(Team::getId)
            .collect(Collectors.toSet());

        return CurrentUserTeamInfo
            .builder()
            .teamIds(teamIds)
            .teamUIDs(teamUids)
            .managedTeamIds(managedTeamIds)
            .managedTeamUIDs(managedTeamUids)
            .userId(user.getId())
            .userUID(user.getUid())
            .build();
    }

    @Cacheable(cacheNames = UserRepository.USER_ACTIVITY_IDS_CACHE, key = "#userLogin")
    public CurrentUserActivityInfo getUserActivityInfo(String userLogin) {
        final var user = userRepository.findOneWithAuthoritiesByLogin(userLogin).orElseThrow(() ->
            new UsernameNotFoundException("User with login " + userLogin + " was not found in the database"));

        final var teams = new HashSet<>(teamRepository.findAllByUserLogin(userLogin, false));

        final var activities = teams.stream()
            .map(Team::getActivity)
            .toList();

        return CurrentUserActivityInfo
            .builder()
            .userId(user.getId())
            .userUID(user.getUid())
            .activityUIDs(activities
                .stream()
                .map(Activity::getUid)
                .collect(Collectors.toSet()))
            .build();
    }

    @Cacheable(cacheNames = UserRepository.USER_TEAM_FORM_ACCESS_CACHE, key = "#userLogin")
    public List<UserFormAccess> getUserFormAccess(String userLogin, Collection<String> teamUIDs) {
        final var user = userRepository.findOneWithAuthoritiesByLogin(userLogin).orElseThrow(() ->
            new UsernameNotFoundException("User with login " + userLogin + " was not found in the database"));
        final var teams = new HashSet<>(teamRepository.findAll(TeamSpecifications.isEnabled()
            .and((root, query, cb) -> root.get("uid").in(teamUIDs))));

        List<UserFormAccess> formAccesses = new ArrayList<>();
        for (final Team team : teams) {
            formAccesses.addAll(team.getFormPermissions()
                .stream()
                .map(formPermissions -> UserFormAccess.builder()
                    .form(formPermissions.getForm())
                    .team(team.getUid())
                    .user(user.getUid())
                    .permissions(formPermissions.getPermissions()).build()).toList());
        }

//        final var formAccess = formAccesses.stream()
//            .collect(Collectors
//                .toMap(UserFormAccess::getForm,
//                    userFormAccess -> userFormAccess));

        return formAccesses;
    }

    @Cacheable(cacheNames = UserRepository.USER_GROUP_IDS_CACHE, key = "#userLogin")
    public CurrentUserGroupInfo getUserGroupIds(String userLogin) {
        final var user = userRepository.findOneWithAuthoritiesByLogin(userLogin).orElseThrow(() ->
            new UsernameNotFoundException("User with login " + userLogin + " was not found in the database"));

        final var ug = new HashSet<>(userGroupRepository.findAllByUserLogin(userLogin, false));
        final var userGroupUids = ug
            .stream()
            .map(UserGroup::getUid)
            .collect(Collectors.toSet());
        final var userGroupIds = ug
            .stream()
            .map(UserGroup::getId)
            .collect(Collectors.toSet());
        return CurrentUserGroupInfo
            .builder()
            .userGroupIds(userGroupIds)
            .userGroupUIDs(userGroupUids)
            .userId(user.getId())
            .userUID(user.getUid())
            .build();
    }
}

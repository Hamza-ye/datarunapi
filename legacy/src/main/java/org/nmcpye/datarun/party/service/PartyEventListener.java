package org.nmcpye.datarun.party.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.party.entities.Party.SourceType;
import org.nmcpye.datarun.party.events.*;
import org.nmcpye.datarun.party.service.PartySyncService.ToSyncParty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

// TODO needs enhancement and polishing
@Component
@RequiredArgsConstructor
@Slf4j
public class PartyEventListener {

    private final PartySyncService syncService;
    private final UserPermissionMaterializationService permissionService;

    // 1. Listen for OrgUnit changes
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onOrgUnitSaved(OrgUnitSavedEvent event) {
        log.debug("Received OrgUnitSavedEvent for UID: {}", event.orgUnit().getUid());
        var ou = event.orgUnit();
        String parentSourceId = (ou.getParent() != null) ? ou.getParent().getId() : null;

        syncService.syncParty(
                ToSyncParty.builder()
                        .id(ou.getId())
                        .uid(ou.getUid())
                        .code(ou.getCode())
                        .name(ou.getName())
                        .label(ou.getLabel())
                        .sourceType(SourceType.ORG_UNIT)
                        .parentId(parentSourceId)
                        .meta(Map.of("level", ou.getLevel(), "path", ou.getPath()))
                        .tags(List.of())
                        .build());
    }

    // 2. Listen for Team changes
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamSaved(TeamSavedEvent event) {
        log.debug("Received TeamSavedEvent for UID: {}", event.team().getUid());
        var team = event.team();

        syncService.syncParty(
                ToSyncParty.builder()
                        .id(team.getId())
                        .uid(team.getUid())
                        .code(team.getCode())
                        .name(team.getName())
                        .sourceType(SourceType.TEAM)
                        .tags(List.of())
                        .parentId(null)
                        .meta(Map.of("description", team.getDescription() != null ? team.getDescription() : ""))
                        .build());
    }

    // 3. Listen for User changes
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserSaved(UserSavedEvent event) {
        log.debug("Received UserSavedEvent for UID: {}", event.user().getUid());
        var user = event.user();

        syncService.syncParty(ToSyncParty.builder()
                .id(user.getId())
                .uid(user.getUid())
                .code(user.getLogin())
                .name(user.getFirstName())
                .sourceType(SourceType.USER)
                .label(Map.of("en", user.getFirstName()))
                .tags(List.of())
                .parentId(null)
                .meta(Map.of())
                .build());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAssignmentMemberChanged(AssignmentMemberChangedEvent event) {
        // This is a simplified incremental update. A more complex system might
        // distinguish between additions and removals. For now, we can trigger
        // a targeted rebuild for the affected users.
        log.info("Assignment membership changed for assignment: {}. Triggering permission update.",
                event.member().getAssignmentId());

        // Option 1 (Simple): Re-calculate all permissions for this one assignment
        // permissionService.rebuildForAssignment(event.member().getAssignmentId());

        // Option 2 (More Targeted): Re-calculate for just the affected member
        permissionService.rebuildForMember(event.member());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onAssignmentBindingChanged(AssignmentBindingChangedEvent event) {
        // This is a simplified incremental update. A more complex system might
        // distinguish between additions and removals. For now, we can trigger
        // a targeted rebuild for the affected users.
        log.info("Assignment membership changed for assignment: {}. Triggering permission update.",
                event.binding().getAssignment().getId());

        // Option 1 (Simple): Re-calculate all permissions for this one assignment
        // permissionService.rebuildForAssignment(event.member().getAssignmentId());

        // Option 2 (More Targeted): Re-calculate for just the affected member
        permissionService.rebuild();
    }

    //

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onTeamUserChanged(TeamUserChangedEvent event) {
        log.debug("Received TeamUserChangedEvent for user UID: {}", event.userUid());
        // We only need to update the User's party, as its tags are what changed.
        syncService.updateUserPartyTags(event.userId());
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onUserGroupUserChanged(UserGroupUserChangedEvent event) {
        log.debug("Received UserGroupUserChangedEvent for user UID: {}", event.userUid());
        syncService.updateUserPartyTags(event.userId());
    }

    // Listen for Activity changes
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onActivitySaved(org.nmcpye.datarun.party.events.ActivitySavedEvent event) {
        log.debug("Received ActivitySavedEvent for UID: {}", event.activity().getUid());
        var activity = event.activity();

        syncService.syncParty(ToSyncParty.builder()
                .id(activity.getId())
                .uid(activity.getUid())
                .code(activity.getCode())
                .name(activity.getName())
                .sourceType(SourceType.ACTIVITY)
                .parentId(null)
                .meta(Map.of())
                .build());
    }
}

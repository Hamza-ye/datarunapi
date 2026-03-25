package org.nmcpye.datarun.party.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.nmcpye.datarun.party.entities.AssignmentMember;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartyResolutionEngine;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.nmcpye.datarun.jooq.public_.Tables.*;

// TODO needs polishing to flow as the flow of access is designed
@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionMaterializationService {

    private final DSLContext dsl;
    private final PartyResolutionEngine partyResolutionEngine;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void rebuild() {
        log.info("Starting full rebuild of user_allowed_party table...");

        // 1. Clean slate
        log.info("Truncating existing permissions...");
        jdbcTemplate.execute("TRUNCATE TABLE user_allowed_party");

        // 2. Fetch all assignment members
        var members = dsl.selectFrom(ASSIGNMENT_MEMBER).fetch();
        log.info("Processing {} assignment member records.", members.size());

        // 3. For each assignment, find its party sets
        var assignmentToPartySetMap = dsl.select(ASSIGNMENT_PARTY_BINDING.ASSIGNMENT_ID,
                ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID)
                .from(ASSIGNMENT_PARTY_BINDING)
                .fetchGroups(ASSIGNMENT_PARTY_BINDING.ASSIGNMENT_ID, ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID);

        // 4. Cache party set expansions (to avoid re-calculating for same set)
        Map<UUID, Set<UUID>> partySetCache = new HashMap<>();

        // 5. Process each assignment member
        for (var member : members) {
            Set<String> userIds = resolveMemberToUserIds(member.getMemberType(), member.getMemberId());
            if (userIds.isEmpty()) {
                continue;
            }

            List<UUID> partySetIds = assignmentToPartySetMap.get(member.getAssignmentId());
            if (partySetIds == null || partySetIds.isEmpty()) {
                continue;
            }

            Set<UUID> allowedPartyIds = new HashSet<>();
            for (UUID partySetId : partySetIds) {
                Set<UUID> parties = partySetCache.computeIfAbsent(partySetId, this::expandPartySet);
                allowedPartyIds.addAll(parties);
            }

            if (!allowedPartyIds.isEmpty()) {
                insertPermissions(userIds, allowedPartyIds, member.getAssignmentId());
            }
        }

        log.info("Full rebuild of user_allowed_party table completed.");
    }

    private Set<String> resolveMemberToUserIds(String memberType, String memberId) {
        if ("USER".equalsIgnoreCase(memberType)) {
            return Set.of(memberId);
        }
        if ("TEAM".equalsIgnoreCase(memberType)) {
            return new HashSet<>(dsl.select(TEAM_USER.USER_ID)
                    .from(TEAM_USER)
                    .where(TEAM_USER.TEAM_ID.eq(memberId))
                    .fetchInto(String.class));
        }
        if ("USER_GROUP".equalsIgnoreCase(memberType)) {
            return new HashSet<>(dsl.select(USER_GROUP_USERS.USER_ID)
                    .from(USER_GROUP_USERS)
                    .where(USER_GROUP_USERS.USER_GROUP_ID.eq(memberId))
                    .fetchInto(String.class));
        }
        return Collections.emptySet();
    }

    private Set<UUID> expandPartySet(UUID partySetId) {
        var partySet = dsl.select(PARTY_SET.KIND, PARTY_SET.SPEC)
                .from(PARTY_SET)
                .where(PARTY_SET.ID.eq(partySetId))
                .fetchOne();

        if (partySet == null)
            return Collections.emptySet();

        PartySetKind kind = PartySetKind.valueOf(partySet.get(PARTY_SET.KIND));
        String spec = partySet.get(PARTY_SET.SPEC).data();
        return Collections.emptySet();

        // commented out, shouldn't it utilize the already built other service for
        // resolving so
        // we keep single source of truth for how things are resolved like:
        // ```java
        // partyResolutionEngine
        // .executeStrategy(kind, partySetId, spec, false,
        // PartyResolutionRequest.builder().build()).stream()
        // .map(ResolvedParty::getId)
        // .collect(Collectors.toSet());
        // ```
        // For a full system-level rebuild, we only handle deterministic kinds.
        // Dynamic kinds like QUERY might depend on runtime context not available here.
        // partyResolutionEngine.executeStrategy(kind, partySetId, );
        // switch (kind) {
        // case STATIC:
        // return new HashSet<>(dsl.select(PARTY_SET_MEMBER.PARTY_ID)
        // .from(PARTY_SET_MEMBER)
        // .where(PARTY_SET_MEMBER.PARTY_SET_ID.eq(partySetId))
        // .fetchInto(UUID.class));
        // case ORG_TREE:
        // // Simplified logic. A more robust solution would parse the spec JSON.
        // // Assuming spec contains `rootId`. This part may need enhancement.
        // UUID rootId =
        // UUID.fromString(partySet.get(PARTY_SET.SPEC).data().split("\"")[3]);
        // return new HashSet<>(dsl.select(PARTY.ID)
        // .from(PARTY) // This would ideally use the org_unit_closure table for
        // performance
        // .where(PARTY.ID.eq(rootId)) // Simplified: just gets the root for now
        // .fetchInto(UUID.class));
        // // TAG_FILTER could also be implemented here.
        // default:
        // log.warn("Skipping expansion of PartySet {} with unhandled kind {} during
        // permission rebuild.", partySetId, kind);
        // return Collections.emptySet();
        // }
    }

    private void insertPermissions(Set<String> userIds, Set<UUID> partyIds, String assignmentId) {
        String sql = "INSERT INTO user_allowed_party (user_id, party_id, permission_mask, provenance, last_updated) VALUES (?, ?, ?, ?::jsonb, ?) ON CONFLICT (user_id, party_id) DO NOTHING";
        List<Object[]> batchArgs = new ArrayList<>();
        String provenanceJson = String.format("{\"source\":\"assignment\", \"assignmentId\":\"%s\"}", assignmentId);

        for (String userId : userIds) {
            for (UUID partyId : partyIds) {
                batchArgs.add(new Object[] {
                        userId,
                        partyId,
                        1, // permission_mask = 1 (VIEW)
                        provenanceJson,
                        Timestamp.from(Instant.now())
                });
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }

    // TODO resolve user from principal of different principal types
    @SuppressWarnings("SqlSourceToSinkFlow")
    public void rebuildForMember(AssignmentMember mem) {
        log.info("Starting rebuild of {}:{} member user_allowed_party table...", mem.getMemberId(),
                mem.getMemberType());

        // 1. Clean slate
        log.info("Deleting existing {}:{} member's permissions...", mem.getMemberId(), mem.getMemberType());
        jdbcTemplate.execute("Delete from user_allowed_party where user_id = " + mem.getMemberId());

        // 2. Fetch all assignment members
        var members = dsl.selectFrom(ASSIGNMENT_MEMBER).fetch();
        log.info("Processing {} assignment member records.", members.size());

        // 3. For each assignment, find its party sets
        var assignmentToPartySetMap = dsl.select(ASSIGNMENT_PARTY_BINDING.ASSIGNMENT_ID,
                ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID)
                .from(ASSIGNMENT_PARTY_BINDING)
                .fetchGroups(ASSIGNMENT_PARTY_BINDING.ASSIGNMENT_ID, ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID);

        // 4. Cache party set expansions (to avoid re-calculating for same set)
        Map<UUID, Set<UUID>> partySetCache = new HashMap<>();

        // 5. Process each assignment member
        Set<String> userIds = resolveMemberToUserIds(mem.getMemberType(), mem.getMemberId());

        if (!userIds.isEmpty()) {
            List<UUID> partySetIds = assignmentToPartySetMap.get(mem.getAssignmentId());
            if (partySetIds != null && !partySetIds.isEmpty()) {
                Set<UUID> allowedPartyIds = new HashSet<>();
                for (UUID partySetId : partySetIds) {
                    Set<UUID> parties = partySetCache.computeIfAbsent(partySetId, this::expandPartySet);
                    allowedPartyIds.addAll(parties);
                }

                if (!allowedPartyIds.isEmpty()) {
                    insertPermissions(userIds, allowedPartyIds, mem.getAssignmentId());
                }
            }

        }

        log.info("Full rebuild of user_allowed_party table completed.");
    }

}

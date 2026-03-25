package org.nmcpye.datarun.party.resolution.engine;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.nmcpye.datarun.party.dto.BindingResult;
import org.nmcpye.datarun.party.dto.CombineMode;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.nmcpye.datarun.jooq.public_.Tables.TEAM;
import static org.nmcpye.datarun.jooq.public_.Tables.USER_GROUP;
import static org.nmcpye.datarun.jooq.public_.tables.Assignment.ASSIGNMENT;
import static org.nmcpye.datarun.jooq.public_.tables.AssignmentPartyBinding.ASSIGNMENT_PARTY_BINDING;
import static org.nmcpye.datarun.jooq.public_.tables.TeamUser.TEAM_USER;
import static org.nmcpye.datarun.jooq.public_.tables.UserGroupUsers.USER_GROUP_USERS;

/**
 * @author Hamza Assada 29/12/2025
 */
@Service
@RequiredArgsConstructor
public class BindingResolver {

    private final DSLContext dsl;

    /**
     * Resolves the list of PartySets to apply based on precedence.
     *
     * @param templateFallbackPartySetId The 'partySetRef' from the
     *                                   vocabulary/template (Level 3 fallback).
     */
    public List<BindingResult> resolveBindings(
            String assignmentId,
            String vocabularyId,
            String role,
            String userId,
            String templateFallbackPartySetId) {
        // 1. Resolve User's Principals (User ID + Team IDs + Group IDs)
        // We do this first to check "Level 1" precedence.
        Set<String> principalIds = getUserPrincipals(userId);

        // 2. LEVEL 1: Check Principal-Scoped Bindings
        // (Assignment + Role + [Vocab?] + Principal)
        List<BindingResult> principalBindings = fetchBindings(assignmentId, vocabularyId, role, principalIds, true);
        if (!principalBindings.isEmpty()) {
            return principalBindings;
        }

        // 3. LEVEL 2: Check Assignment-Global Bindings
        // (Assignment + Role + [Vocab?] + No Principal)
        List<BindingResult> globalBindings = fetchBindings(assignmentId, vocabularyId, role, null, false);
        if (!globalBindings.isEmpty()) {
            return globalBindings;
        }

        // 4. LEVEL 3: Template Fallback
        if (templateFallbackPartySetId != null) {
            return List.of(new BindingResult(
                    templateFallbackPartySetId,
                    CombineMode.UNION,
                    "Template Fallback (partySetRef)"));
        }

        // 5. LEVEL 4: Assignment Default
        UUID assignmentDefaultUuid = dsl.select(ASSIGNMENT.DEFAULT_PARTY_SET_ID)
                .from(ASSIGNMENT)
                .where(ASSIGNMENT.ID.eq(assignmentId)) // NOTE: assuming assignmentId in ASSIGNMENT is still String or
                                                       // we need to cast? ASSIGNMENT.ID is String. Wait, ASSIGNMENT.ID
                                                       // in Jooq might be UUID? no we know Assignment is using String
                                                       // id.
                .fetchOneInto(UUID.class);

        String assignmentDefaultId = assignmentDefaultUuid != null ? assignmentDefaultUuid.toString() : null;

        if (assignmentDefaultId != null) {
            return List.of(new BindingResult(
                    assignmentDefaultId,
                    CombineMode.UNION,
                    "Assignment Default"));
        }

        // 6. No bindings found (return empty list, effectively no parties)
        return Collections.emptyList();
    }

    /**
     * for superuser, fetch with no per principle filtering
     *
     * @param assignmentId assignment id
     * @param vocabularyId template id
     * @param role         binding role name
     * @return admin accessible binding, which is all
     */
    public List<BindingResult> resolveBindings(String assignmentId, String vocabularyId, String role) {
        return fetchBindings(assignmentId, vocabularyId, role, null, false);
    }

    /**
     * Fetches bindings from ASSIGNMENT_PARTY_BINDING table.
     * Handles the precedence of "Specific Vocabulary" > "Null Vocabulary"
     * implicitly via sorting.
     */
    private List<BindingResult> fetchBindings(
            String assignmentId,
            String vocabularyId,
            String role,
            Set<String> principalIds,
            boolean usePrincipals) {
        var query = dsl.select(
                ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID,
                ASSIGNMENT_PARTY_BINDING.COMBINE_MODE,
                ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID,
                ASSIGNMENT_PARTY_BINDING.PRINCIPAL_ID)
                .from(ASSIGNMENT_PARTY_BINDING)
                .where(ASSIGNMENT_PARTY_BINDING.ASSIGNMENT_ID.eq(assignmentId))
                .and(ASSIGNMENT_PARTY_BINDING.NAME.eq(role));

        // Filter by Principals or NULL
        if (usePrincipals && principalIds != null && !principalIds.isEmpty()) {
            query = query.and(ASSIGNMENT_PARTY_BINDING.PRINCIPAL_ID.in(principalIds));
        } else {
            query = query.and(ASSIGNMENT_PARTY_BINDING.PRINCIPAL_ID.isNull());
        }

        // Handle Vocabulary Specificity
        // We want rows that match our Vocab OR are NULL.
        if (vocabularyId != null) {
            query = query.and(
                    ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID.eq(vocabularyId)
                            .or(ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID.isNull()));
        } else {
            query = query.and(ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID.isNull());
        }

        // Fetch all candidates
        var results = query.fetch();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        // In-Memory Sorting for Specificity (avoiding complex SQL sorting for now)
        // Rule: Specific Vocab > Null Vocab.
        // If we have Mixed Specificity (some match vocab, some are null),
        // the "Specific Vocabulary" rule usually strictly wins in config systems.
        // Strategy: If *any* row matches the specific Vocabulary, discard the
        // Null-Vocab rows.
        boolean hasSpecificVocabMatch = results.stream()
                .anyMatch(r -> vocabularyId != null &&
                        vocabularyId.equals(r.get(ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID)));

        return results.stream()
                .filter(r -> {
                    if (!hasSpecificVocabMatch)
                        return true; // Keep all if no specific winner
                    return vocabularyId != null && vocabularyId.equals(r.get(ASSIGNMENT_PARTY_BINDING.VOCABULARY_ID));
                })
                .map(r -> new BindingResult(
                        String.valueOf(r.get(ASSIGNMENT_PARTY_BINDING.PARTY_SET_ID)),
                        // Ensure DB stores 'UNION'/'INTERSECT'
                        CombineMode.valueOf(r.get(ASSIGNMENT_PARTY_BINDING.COMBINE_MODE) != null
                                ? r.get(ASSIGNMENT_PARTY_BINDING.COMBINE_MODE)
                                : "UNION"),
                        determineProvenance(r, usePrincipals)))
                .collect(Collectors.toList());
    }

    private String determineProvenance(Record r, boolean isPrincipal) {
        if (isPrincipal)
            return "Principal Binding: " + r.get(ASSIGNMENT_PARTY_BINDING.PRINCIPAL_ID);
        return "Assignment Global Binding";
    }

    /**
     * Helper to gather all IDs representing the user (User ID + Team IDs + Group
     * IDs).
     */
    private Set<String> getUserPrincipals(String userId) {
        Set<String> principals = new java.util.HashSet<>();
        principals.add(userId);

        // Add Teams
        Set<String> teams = new HashSet<>(dsl.select(TEAM_USER.TEAM_ID)
                .from(TEAM_USER)
                .join(TEAM).on(TEAM_USER.TEAM_ID.eq(TEAM.ID))
                .where(TEAM_USER.USER_ID.eq(userId).and(TEAM.DISABLED.isFalse().or(TEAM.DISABLED.isNull())))
                .fetchInto(String.class));
        principals.addAll(teams);

        // Add User Groups
        Set<String> groups = new HashSet<>(dsl.select(USER_GROUP_USERS.USER_GROUP_ID)
                .from(USER_GROUP_USERS)
                .join(USER_GROUP).on(USER_GROUP_USERS.USER_GROUP_ID.eq(USER_GROUP.ID))
                .where(USER_GROUP_USERS.USER_ID.eq(userId).and(USER_GROUP.DISABLED.isFalse()
                        .or(USER_GROUP.DISABLED.isNull())))
                .fetchInto(String.class));

        principals.addAll(groups);

        return principals;
    }
}

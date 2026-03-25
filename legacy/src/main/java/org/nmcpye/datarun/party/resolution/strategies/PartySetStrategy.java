package org.nmcpye.datarun.party.resolution.strategies;

import org.jooq.SelectConditionStep;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.nmcpye.datarun.jooq.public_.Tables.PARTY;

/**
 * @author Hamza Assada 29/12/2025
 */
public interface PartySetStrategy {

    /**
     * @return The specific kind this strategy handles (STATIC, ORG_TREE, etc.)
     */
    PartySetKind getKind();

    /**
     * Resolves parties based on the party set specification.
     *
     * @param partySetId The ID of the configuration row (party_set)
     * @param spec       The JSONB spec payload (parsed or raw)
     * @param request    The user's request context
     * @return A list of resolved parties
     */
    List<ResolvedParty> resolve(String partySetId,
            String spec,
            boolean isMaterialized,
            PartyResolutionRequest request);

    /**
     * Applies the since filter to an ongoing query.
     *
     * @param query The base query (selecting from PARTY or joining PARTY)
     * @param since last modified since this time
     * @return The query with the since filter applied
     */
    default <R extends org.jooq.Record> SelectConditionStep<R> applySinceFilter(SelectConditionStep<R> query,
            Instant since) {
        if (since == null) {
            return query;
        }

        return query
                .and(PARTY.LAST_MODIFIED_DATE.greaterThan(LocalDateTime.ofInstant(since, ZoneId.systemDefault())));
    }
}

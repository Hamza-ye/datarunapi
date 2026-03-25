package org.nmcpye.datarun.party.resolution.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartySecurityFilter;
import org.nmcpye.datarun.party.service.JooqMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.nmcpye.datarun.jooq.public_.Tables.PARTY_TAG;
import static org.nmcpye.datarun.jooq.public_.tables.Party.PARTY;
import static org.nmcpye.datarun.jooq.public_.tables.PartySetMember.PARTY_SET_MEMBER;

/**
 * @author Hamza Assada 29/12/2025
 */
@Component
@RequiredArgsConstructor
public class StaticPartySetStrategy implements PartySetStrategy {

    private final DSLContext dsl;
    private final PartySecurityFilter securityFilter; // Injected
    private final ObjectMapper objectMapper;
    private final JooqMapper jooqMapper;

    @Override
    public PartySetKind getKind() {
        return PartySetKind.STATIC;
    }

    @Override
    public List<ResolvedParty> resolve(String partySetId,
            String spec,
            boolean isMaterialized,
            PartyResolutionRequest request) {

        // 1. Base Condition: Must belong to this specific set
        Condition whereCondition = PARTY_SET_MEMBER.PARTY_SET_ID.eq(UUID.fromString(partySetId));

        // 2. Apply Search (if provided)
        // We check name or code via case-insensitive search
        if (request.getSearchQuery() != null && !request.getSearchQuery().isBlank()) {
            String term = "%" + request.getSearchQuery().trim() + "%";
            whereCondition = whereCondition.and(
                    PARTY.NAME.likeIgnoreCase(term)
                            .or(PARTY.CODE.likeIgnoreCase(term)));
        }

        var query = dsl.select(
                PARTY.ID,
                PARTY.UID,
                PARTY.TYPE,
                PARTY.NAME,
                PARTY.CODE,
                PARTY_TAG.META,
                PARTY.SOURCE_TYPE)
                .from(PARTY)
                .join(PARTY_SET_MEMBER).on(PARTY.ID.eq(PARTY_SET_MEMBER.PARTY_ID))
                .join(PARTY_TAG).on(PARTY.ID.eq(PARTY_TAG.PARTY_ID))
                .where(whereCondition);

        // --- APPLY modified since FILTER ---
        query = applySinceFilter(query, request.getSince());

        // --- APPLY SECURITY FILTER ---
        var securedQuery = securityFilter.apply(query, request.getUserId(), isMaterialized);

        // 3. Execute Query
        return securedQuery
                .orderBy(PARTY.NAME.asc())
                .limit(request.getLimit())
                .offset(request.getOffset())
                .fetch(jooqMapper::mapPartyRecord);
    }
}

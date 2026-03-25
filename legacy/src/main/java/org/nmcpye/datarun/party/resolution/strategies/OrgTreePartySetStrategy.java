package org.nmcpye.datarun.party.resolution.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartySecurityFilter;
import org.nmcpye.datarun.party.service.JooqMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.nmcpye.datarun.jooq.public_.tables.Party.PARTY;

@Component
@RequiredArgsConstructor
public class OrgTreePartySetStrategy implements PartySetStrategy {

    private final DSLContext dsl;
    private final PartySecurityFilter securityFilter;
    private final ObjectMapper objectMapper;
    private final JooqMapper jooqMapper;

    @Override
    public PartySetKind getKind() {
        return PartySetKind.ORG_TREE;
    }

    @Override
    public List<ResolvedParty> resolve(String partySetId,
            String specJson,
            boolean isMaterialized,
            PartyResolutionRequest request) {
        // 1. Parse Spec
        // Expected Spec: { "rootId": "uuid-of-root", "depth": 5, "includeSelf": true }
        JsonNode spec = null;
        try {
            spec = objectMapper.readTree(specJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        String rootIdStr = spec.path("rootId").asText();
        if (rootIdStr == null || rootIdStr.isBlank()) {
            // Cannot proceed without a root
            return Collections.emptyList();
        }
        UUID rootId = UUID.fromString(rootIdStr);
        int depth = spec.path("depth").asInt(100); // Default to a high depth if not specified
        boolean includeSelf = spec.path("includeSelf").asBoolean(true);

        // 2. Define Recursive CTE
        Table<?> hierarchy = buildHierarchyCte(rootId, depth);
        Field<UUID> hierarchyIdField = hierarchy.field("id", UUID.class);

        // 3. Get all IDs from the hierarchy first
        Set<UUID> partyIdsInTree = dsl.select(hierarchyIdField)
                .from(hierarchy)
                .fetchSet(hierarchyIdField);

        if (!includeSelf) {
            partyIdsInTree.remove(rootId);
        }

        if (partyIdsInTree.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. Build the final query against the PARTY table
        var query = dsl.selectFrom(PARTY)
                .where(PARTY.ID.in(partyIdsInTree));

        // 5. Apply Search Filter (if provided)
        if (request.getSearchQuery() != null && !request.getSearchQuery().isBlank()) {
            String term = "%" + request.getSearchQuery().trim() + "%";
            query = query.and(PARTY.NAME.likeIgnoreCase(term).or(PARTY.CODE.likeIgnoreCase(term)));
        }

        // 6. Apply Security Filter
        query = applySinceFilter(query, request.getSince());

        // 7. Apply Security Filter
        var securedQuery = securityFilter.apply(query, request.getUserId(), isMaterialized);

        // 8. Execute, Paginate, and Map
        return securedQuery
                .orderBy(PARTY.NAME.asc())
                .limit(request.getLimit())
                .offset(request.getOffset())
                .fetch(jooqMapper::mapPartyRecord);

    }

    private Table<?> buildHierarchyCte(UUID rootId, int depth) {
        return DSL.table(
                DSL.sql(
                        """
                                WITH RECURSIVE hierarchy(id, parent_id, level) AS (
                                  SELECT id, parent_id, 1 FROM party WHERE id = ?
                                  UNION ALL
                                  SELECT p.id, p.parent_id, h.level + 1
                                  FROM party p
                                  JOIN hierarchy h ON p.parent_id = h.id
                                  WHERE h.level < ?
                                )
                                SELECT id FROM hierarchy
                                """,
                        rootId,
                        depth));
    }
}

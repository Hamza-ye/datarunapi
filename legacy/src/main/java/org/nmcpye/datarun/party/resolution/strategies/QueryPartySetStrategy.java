package org.nmcpye.datarun.party.resolution.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.SelectConditionStep;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartySecurityFilter;
import org.nmcpye.datarun.party.service.JooqMapper;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.nmcpye.datarun.jooq.public_.Tables.PARTY;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryPartySetStrategy implements PartySetStrategy {

    private final NamedQueryProvider queryProvider;
    private final PartySecurityFilter securityFilter; // Injected
    private final JooqMapper jooqMapper;
    private final ObjectMapper objectMapper;

    @Override
    public PartySetKind getKind() {
        return PartySetKind.QUERY;
    }

    @Override
    public List<ResolvedParty> resolve(String partySetId,
            String specJson,
            boolean isMaterialized,
            PartyResolutionRequest request) {
        try {
            // 1. Parse Spec
            // Expected Spec: { "query": "FIND_ORG_CHILDREN", "params": { "parentId":
            // "region_id" } }
            JsonNode spec = objectMapper.readTree(specJson);
            String queryName = spec.path("sqlKey").asText();
            JsonNode paramMapping = spec.path("params");

            // 2. Resolve Parameters (Map Spec Param -> Context Value)
            Map<String, Object> resolvedParams = new HashMap<>();
            if (paramMapping.isObject()) {
                paramMapping.fields().forEachRemaining(entry -> {
                    String sqlParamName = entry.getKey();
                    String contextKey = entry.getValue().asText();

                    // Look up value in the Request Context (e.g. what the user selected in dropdown
                    // A)
                    Object contextValue = request.getContextValues().get(contextKey);
                    resolvedParams.put(sqlParamName, contextValue);
                });
            }

            // 3. Get Base Query
            SelectConditionStep<?> baseQuery = queryProvider.getQuery(queryName, resolvedParams);

            if (baseQuery == null) {
                // Pre-requisites not met (e.g. parent dropdown not selected yet)
                return Collections.emptyList();
            }

            // 4. Apply Common Filters (Search q)
            if (request.getSearchQuery() != null && !request.getSearchQuery().isBlank()) {
                String term = "%" + request.getSearchQuery().trim() + "%";
                baseQuery.and(
                        PARTY.NAME.likeIgnoreCase(term).or(PARTY.CODE.likeIgnoreCase(term)));
            }

            // Apply Security Filter
            baseQuery = applySinceFilter(baseQuery, request.getSince());

            // --- APPLY SECURITY FILTER HERE ---
            // Note: The baseQuery MUST select from PARTY (or alias it correctly) for the
            // filter to work.
            // The NamedQueryProvider should ensure queries are rooted in PARTY.
            var securedQuery = securityFilter.apply(baseQuery, request.getUserId(), isMaterialized);

            // 5. Execute & Map
            // Note: baseQuery is selecting *, so we can fetch into ResolvedParty manually
            return securedQuery
                    .orderBy(PARTY.NAME.asc())
                    .limit(request.getLimit())
                    .offset(request.getOffset())
                    .fetch(jooqMapper::mapPartyRecord);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid JSON spec for PartySet " + partySetId, e);
        }
    }
}

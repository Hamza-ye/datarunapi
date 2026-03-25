package org.nmcpye.datarun.party.resolution.strategies;

import org.jooq.DSLContext;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.nmcpye.datarun.jooq.public_.Tables.PARTY;

@Component
public class NamedQueryProvider {

    private final DSLContext dsl;
    private final Map<String, BiFunction<DSLContext, Map<String, Object>, SelectConditionStep<?>>> queries;

    public NamedQueryProvider(DSLContext dsl) {
        this.dsl = dsl;
        this.queries = Map.of(
            // Query 1: Find children of a specific Parent Org Unit
            // Spec: { "sqlKey": "FIND_ORG_CHILDREN", "params": { "parentId": "context_region_id" } }
            "FIND_ORG_CHILDREN", (ctx, params) -> {
                UUID parentId = getUuid(params, "parentId");
                if (parentId == null) return null; // Or throw, or return empty condition

                // We join Party to OrgUnit to filter by parent
                return ctx.select(PARTY.asterisk())
                    .from(PARTY)
                    .where(PARTY.PARENT_ID.eq(parentId));
            },

            // Query 2: Find Teams managing a specific Team (e.g. for escalation)
            "FIND_MANAGING_TEAMS", (ctx, params) -> {
                UUID teamId = getUuid(params, "targetTeamId");
                // ... logic ...
                return ctx.select(PARTY.asterisk())
                    .from(PARTY)
                    .where(DSL.falseCondition()); // stub
            },
            // Query 2: Example of finding parties of a certain type with a name like a parameter.
            // Spec Params: { "partyType": "TEAM", "nameQuery": "search-term" }
            "FIND_PARTIES_BY_TYPE_AND_NAME", (ctx, params) -> {
                String partyType = getString(params, "partyType");
                String nameQuery = getString(params, "nameQuery");
                if (partyType == null || nameQuery == null) return null;

                return dsl.selectFrom(PARTY)
                    .where(PARTY.TYPE.eq(partyType))
                    .and(PARTY.NAME.likeIgnoreCase("%" + nameQuery + "%"));
            }
        );
    }

    public SelectConditionStep<?> getQuery(String queryName, Map<String, Object> resolvedParams) {
        if (!queries.containsKey(queryName)) {
            throw new IllegalArgumentException("Unknown named query: " + queryName);
        }
        return queries.get(queryName).apply(dsl, resolvedParams);
    }

    // Helper methods to safely extract and cast parameters
    private UUID getUuid(Map<String, Object> params, String key) {
        Object val = params.get(key);
        if (val instanceof String s && !s.isBlank()) return UUID.fromString(s);
        if (val instanceof UUID u) return u;
        return null;
    }

    private String getString(Map<String, Object> params, String key) {
        Object val = params.get(key);
        return (val != null) ? val.toString() : null;
    }
}

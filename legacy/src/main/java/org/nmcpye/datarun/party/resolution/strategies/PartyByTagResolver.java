package org.nmcpye.datarun.party.resolution.strategies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Resolver for party_set kind=QUERY with sqlKey = "party_by_tag".
 * Provides two resolver variants:
 * - resolveWithJooq(...)  -> uses jOOQ DSL
 * - resolveWithJdbc(...)  -> uses NamedParameterJdbcTemplate
 * <p>
 * Spec contract:
 * spec.params.tags = [ { "key":"assigned_to_team", "value":":teamUid" }, ... ]
 * <p>
 * Runtime params map contains values to replace placeholders (without colon): e.g. { "teamUid" : "TEAM_A" }
 * <p>
 * Behavior:
 * - tag predicates are ANDed (intersection)
 * - placeholders with missing runtime value are ignored (optional)
 * - supports q (name ilike), type (party.type) and activeOnly (p.active boolean) filters
 */
@Component
@RequiredArgsConstructor
public class PartyByTagResolver {

    private final DSLContext dsl;
    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper;

    // ---------------------------------------
    // Public entry using jOOQ
    // ---------------------------------------
    public List<Map<String, Object>> resolveWithJooq(JsonNode partySetSpec,
                                                     Map<String, Object> runtimeParams,
                                                     String q,
                                                     String type,
                                                     Boolean activeOnly,
                                                     int limit,
                                                     int offset) {
        // quick guard: expect sqlKey == party_by_tag
        String sqlKey = specSqlKey(partySetSpec);
        if (!"party_by_tag".equals(sqlKey)) return Collections.emptyList();

        List<Map<String, String>> tagFilters = buildTagFilters(partySetSpec, runtimeParams);
        Table<?> p = DSL.table(DSL.name("party")).as("p");
        var base = dsl.select(
            DSL.field(DSL.name("p", "id")),
            DSL.field(DSL.name("p", "uid")),
            DSL.field(DSL.name("p", "type")),
            DSL.field(DSL.name("p", "code")),
            DSL.field(DSL.name("p", "name"))
        ).from(p);

        // dynamic joins (one alias per tag filter)
        int idx = 0;
        for (Map<String, String> tag : tagFilters) {
            idx++;
            String alias = "pt" + idx;
            Table<?> pt = DSL.table(DSL.name("party_tag")).as(alias);
            base = base.join(pt)
                .on(DSL.field(DSL.name(alias, "party_id")).eq(DSL.field(DSL.name("p", "id"))))
                .and(DSL.field(DSL.name(alias, "tag_key")).eq(tag.get("key")))
                .and(DSL.field(DSL.name(alias, "tag_value")).eq(tag.get("value")));
        }

        // where
        org.jooq.Condition cond = DSL.trueCondition();
        if (q != null && !q.isBlank()) {
//            cond = cond.and(DSL.field(DSL.name("p", "name")).ilike("%" + q + "%"));
            cond = cond.and(DSL.field(DSL.name("p", "name")).notLike("%" + q + "%"));
        }
        if (type != null && !type.isBlank()) {
            cond = cond.and(DSL.field(DSL.name("p", "type")).eq(type));
        }
        if (activeOnly != null) {
            cond = cond.and(DSL.field(DSL.name("p", "active")).eq(activeOnly));
        }

        var finalQuery = base.where(cond)
            .orderBy(DSL.field(DSL.name("p", "name")), DSL.field(DSL.name("p", "id")))
            .limit(limit)
            .offset(offset);

        return finalQuery.fetchMaps();
    }

    // ---------------------------------------
    // Public entry using NamedParameterJdbcTemplate
    // ---------------------------------------
    public List<Map<String, Object>> resolveWithJdbc(JsonNode partySetSpec,
                                                     Map<String, Object> runtimeParams,
                                                     String q,
                                                     String type,
                                                     Boolean activeOnly,
                                                     int limit,
                                                     int offset) {
        String sqlKey = specSqlKey(partySetSpec);
        if (!"party_by_tag".equals(sqlKey)) return Collections.emptyList();

        List<Map<String, String>> tagFilters = buildTagFilters(partySetSpec, runtimeParams);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id, p.uid, p.type, p.code, p.name FROM party p ");

        Map<String, Object> params = new HashMap<>();
        int idx = 0;
        for (Map<String, String> tag : tagFilters) {
            idx++;
            String alias = "pt" + idx;
            sql.append(" JOIN party_tag ").append(alias)
                .append(" ON ").append(alias).append(".party_id = p.id")
                .append(" AND ").append(alias).append(".tag_key = :tagKey").append(idx)
                .append(" AND ").append(alias).append(".tag_value = :tagVal").append(idx);
            params.put("tagKey" + idx, tag.get("key"));
            params.put("tagVal" + idx, tag.get("value"));
        }

        sql.append(" WHERE 1=1 ");
        if (q != null && !q.isBlank()) {
            sql.append(" AND p.name ILIKE :q ");
            params.put("q", "%" + q + "%");
        }
        if (type != null && !type.isBlank()) {
            sql.append(" AND p.type = :type ");
            params.put("type", type);
        }
        if (activeOnly != null) {
            sql.append(" AND p.active = :activeOnly ");
            params.put("activeOnly", activeOnly);
        }

        sql.append(" ORDER BY p.name, p.id LIMIT :limit OFFSET :offset ");
        params.put("limit", limit);
        params.put("offset", offset);

        return jdbc.queryForList(sql.toString(), params);
    }

    // ---------------------------------------
    // Helper: buildTagFilters
    // Reads spec.params.tags (array) and substitutes placeholders using runtimeParams.
    // If a tag value is a placeholder (startsWith ':'), we resolve it; if unresolved -> skip tag.
    // If tag value is literal -> include as-is.
    // ---------------------------------------
    public List<Map<String, String>> buildTagFilters(JsonNode partySetSpec, Map<String, Object> runtimeParams) {
        List<Map<String, String>> out = new ArrayList<>();
        if (partySetSpec == null) return out;
        JsonNode params = partySetSpec.path("params");
        if (params.isMissingNode()) return out;
        JsonNode tags = params.path("tags");
        if (!tags.isArray()) return out;

        for (JsonNode tagNode : tags) {
            String key = tagNode.path("key").asText(null);
            String value = tagNode.path("value").asText(null);
            if (key == null || value == null) continue;

            String resolved = resolvePlaceholder(value, runtimeParams);
            if (resolved == null) {
                // skip unresolved tag (treat as optional)
                continue;
            }
            Map<String, String> m = new HashMap<>();
            m.put("key", key);
            m.put("value", resolved);
            out.add(m);
        }
        return out;
    }

    // resolve placeholder like ":teamUid" or return literal if not prefixed
    private String resolvePlaceholder(String value, Map<String, Object> runtimeParams) {
        if (value.startsWith(":")) {
            String paramName = value.substring(1);
            Object v = runtimeParams == null ? null : runtimeParams.get(paramName);
            return v == null ? null : v.toString();
        } else {
            return value;
        }
    }

    // helper to read sqlKey from spec node (safe)
    private String specSqlKey(JsonNode spec) {
        if (spec == null) return null;
        JsonNode key = spec.path("sqlKey");
        return key.isTextual() ? key.asText() : null;
    }
}

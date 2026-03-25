package org.nmcpye.datarun.party.resolution.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.nmcpye.datarun.party.dto.PartyResolutionRequest;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.nmcpye.datarun.party.entities.PartySetKind;
import org.nmcpye.datarun.party.resolution.engine.PartySecurityFilter;
import org.nmcpye.datarun.party.service.JooqMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TagFilterPartySetStrategy implements PartySetStrategy {

    private final DSLContext dsl;
    private final PartySecurityFilter securityFilter;
    private final ObjectMapper objectMapper;
    private final JooqMapper jooqMapper;

    @Override
    public PartySetKind getKind() {
        return PartySetKind.TAG_FILTER;
    }

    @Override
    public List<ResolvedParty> resolve(String partySetId,
            String specJson,
            boolean isMaterialized,
            PartyResolutionRequest request) {
        return Collections.emptyList();
        // try {
        // // 1. Parse Spec
        // // Expected Spec: { "tags": ["tag1", "tag2"], "types": ["ORG_UNIT", "TEAM"] }
        // JsonNode spec = objectMapper.readTree(specJson);
        //
        // List<String> requiredTags = new ArrayList<>();
        // JsonNode tagsNode = spec.path("tags");
        // if (tagsNode.isArray()) {
        // tagsNode.forEach(node -> requiredTags.add(node.asText()));
        // }
        //
        // List<String> requiredTypes = new ArrayList<>();
        // JsonNode typesNode = spec.path("types");
        // if (typesNode.isArray()) {
        // typesNode.forEach(node -> requiredTypes.add(node.asText()));
        // }
        //
        // // 2. Build the base query
        // var query = dsl.selectFrom(PARTY)
        // // 3. Apply Tag Filter Condition
        // // This query uses the JSONB @> operator, which is highly efficient with a
        // GIN index.
        // .where(!requiredTags.isEmpty() ? PARTY.TAGS.contains(
        // JSONB.valueOf(objectMapper.writeValueAsString(requiredTags))) :
        // DSL.falseCondition());
        //
        // // 4. Apply Type Filter Condition (if specified)
        // if (!requiredTypes.isEmpty()) {
        // query = query.and(PARTY.SOURCE_TYPE.in(requiredTypes));
        // }
        //
        // // 5. Apply Search Filter (if provided)
        // if (request.getSearchQuery() != null && !request.getSearchQuery().isBlank())
        // {
        // String term = "%" + request.getSearchQuery().trim() + "%";
        // query =
        // query.and(PARTY.NAME.likeIgnoreCase(term).or(PARTY.CODE.likeIgnoreCase(term)));
        // }
        //
        // // --- APPLY modified since FILTER ---
        // query = applySinceFilter(query, request.getSince());
        //
        // // 6. Apply Security Filter
        // var securedQuery = securityFilter.apply(query, request.getUserId(),
        // isMaterialized);
        //
        // // 7. Execute, Paginate, and Map
        // return securedQuery
        // .orderBy(PARTY.NAME.asc())
        // .limit(request.getLimit())
        // .offset(request.getOffset())
        // .fetch(jooqMapper::mapPartyRecord);
        //
        // } catch (JsonProcessingException e) {
        // throw new RuntimeException("Invalid JSON spec for TAG_FILTER PartySet " +
        // partySetId, e);
        // }
    }
}

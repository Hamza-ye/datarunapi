package org.nmcpye.datarun.party.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.party.dto.ResolvedParty;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static org.nmcpye.datarun.jooq.public_.tables.Party.PARTY;

@Service
@RequiredArgsConstructor
@Slf4j
public class JooqMapper {
    private final ObjectMapper objectMapper;

    public ResolvedParty mapPartyRecord(org.jooq.Record r) {
        Map<String, Object> properties = new HashMap<>();
//        try {
//            properties = objectMapper.readValue(r.get(PARTY.PROPERTIES_MAP).data(),
//                new TypeReference<Map<String, Object>>() {
//                });
//        } catch (JsonProcessingException e) {
//            log.error("error reading properties map", e);
//        }

        return ResolvedParty.builder()
            .id(r.get(PARTY.ID))
            .uid(r.get(PARTY.UID))
            .code(r.get(PARTY.CODE))
            .type(r.get(PARTY.TYPE))
            .name(r.get(PARTY.NAME))
            .properties(properties)
            .source(r.get(PARTY.SOURCE_TYPE))
            .build();
    }
}

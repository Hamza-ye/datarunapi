package org.nmcpye.datarun.party.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.party.dto.PartySetDto;
import org.nmcpye.datarun.party.entities.PartySet;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartySetMapper {
    private final ObjectMapper objectMapper;

    public PartySetDto toDto(PartySet entity) {
        if (entity == null)
            return null;
        return PartySetDto.builder()
                .id(entity.getId())
                .uid(entity.getUid())
                .name(entity.getName())
                .code(entity.getCode())
                .kind(entity.getKind())
                .spec(objectMapper.valueToTree(entity.getSpec())) // Convert spec object to JsonNode
                .isMaterialized(entity.getIsMaterialized())
                .build();
    }

    public PartySet toEntity(PartySetDto dto) {
        if (dto == null)
            return null;
        PartySet.PartySetSpec specObject = objectMapper.convertValue(dto.getSpec(), PartySet.PartySetSpec.class);
        PartySet entity = PartySet.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .kind(dto.getKind())
                .spec(specObject) // Convert JsonNode back to spec object
                .isMaterialized(dto.getIsMaterialized())
                .build();
        entity.setId(dto.getId());
        entity.setUid(dto.getUid());
        return entity;
    }
}

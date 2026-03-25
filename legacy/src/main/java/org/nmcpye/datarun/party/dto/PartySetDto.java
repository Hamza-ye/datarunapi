package org.nmcpye.datarun.party.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;
import org.nmcpye.datarun.party.entities.PartySetKind;

import java.time.Instant;

@Data
@Builder
public class PartySetDto {
    private String id;
    private String uid;
    private String name;
    private String code;
    private PartySetKind kind;
    private JsonNode spec; // Represent spec as a flexible JSON object in the API
    private Boolean isMaterialized;
    private String createdBy;
    private Instant createdDate;
    private String lastModifiedBy;
    private Instant lastModifiedDate;
}

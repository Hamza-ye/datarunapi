package org.nmcpye.datarun.party.dto;

import lombok.Value;
import org.nmcpye.datarun.party.entities.PartySet.PartySetSpec;
import org.nmcpye.datarun.party.entities.PartySetKind;

import java.util.UUID;

@Value
public class PartySetConfig {
    UUID id;
    PartySetKind kind;
    PartySetSpec spec; // JSONB
}

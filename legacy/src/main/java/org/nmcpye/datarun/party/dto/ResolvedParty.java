package org.nmcpye.datarun.party.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/// A lightweight projection of a Party, separate from the full JPA entity
///
/// @author Hamza Assada 28/12/2025
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolvedParty {
    UUID id;
    String uid;
    String type;
    String name; // name
    String code;
    Map<String, Object> properties; // jsonb payload
    String source; // INTERNAL vs EXTERNAL
}

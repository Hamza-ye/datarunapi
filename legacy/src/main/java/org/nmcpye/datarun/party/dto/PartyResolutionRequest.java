package org.nmcpye.datarun.party.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * @author Hamza Assada 29/12/2025
 */
@Value
@Builder
public class PartyResolutionRequest {
    /// Context
    String assignmentId;

    String userId;
    /// e.g., "sender", "receiver"
    String role;
    /// Optional, can be null
    String vocabularyId;

    /// Runtime filters "q"
    String searchQuery;

    Instant since;

    int limit;
    int offset;

    /// Dynamic values from the form (for Dependent Selects)
    /// e.g., "region_id" -> "uuid..."
    @Builder.Default
    Map<String, Object> contextValues = Map.of();
}

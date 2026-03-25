package org.nmcpye.datarun.party.dto;

import lombok.Value;

/**
 * @author Hamza Assada 29/12/2025
 */
@Value
public class BindingResult {
    String partySetId;
    CombineMode combineMode;
    /**
     * For debugging/audit: "Role Binding (Team X)", "Assignment Default", etc.
     */
    String provenance;
}

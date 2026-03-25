package org.nmcpye.datarun.party.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// The "Manifest" DTO: Everything the client needs to initialize a context.
///
/// @author Hamza Assada 28/12/2025
@Data
@Builder(toBuilder = true)
public class AssignmentManifestDto {
    @NotNull
    private String assignmentUid;
    private String label;
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.PENDING; // IN_PROGRESS, DONE, EXPIRED, CANCELLED

    /// allowedTemplateUids
    private Set<String> allowedTemplateUids;
    private List<BindingDto> bindings;

    // legacy
    @Builder.Default
    private Boolean deleted = false;
    private Integer startDay;
    private String orgUnitUid;
    private String teamUid;
    private String activityUid;

    // legacy All templates
    @Builder.Default
    private Set<String> forms = new HashSet<>();

    private String defaultPartySet;

    @Data
    @Builder
    public static class BindingDto {
        /// `Null` if global for assignment
        private String templateUid;
        private String roleName;
        private String partySetId;

        CombineMode combineMode;
        /// For debugging/audit: "Role Binding (Team X)", "Assignment Default", etc.
        String provenance;
    }
}

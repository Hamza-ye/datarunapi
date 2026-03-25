package org.nmcpye.datarun.party.dto;

import org.nmcpye.datarun.sharedkernal.enumeration.AccessLevel;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/// Control which `data_template` (vocabulary) a *principal* (user/team/group or a `member role`) can see/use inside
/// a specific assignment. e.g. preventing HF users from seeing `Issue` while letting MU officers see it — without hacks.
///
/// @author Hamza Assada 06/01/2026
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AssignmentRoleDataPolicyModel {
    private String id;
    private String uid;
    private String code;
    private String name;

    @NotNull
    private String assignmentId;

    /// Precedence Logic: The AssignmentRolePartyPolicy entity correctly allows
    /// vocabularyId to be nullable,
    /// enabling the "Global Role" vs "Specific Form Role" logic.
    /// Adaptability: We are using vocabularyId in the DB, but we use DataTemplate
    /// in our Services/Mappers
    /// to keep it consistent with our existing code.
    @NotNull
    private String dataTemplateId;

    private String role;

    @NotNull
    private AccessLevel accessLevel;
}

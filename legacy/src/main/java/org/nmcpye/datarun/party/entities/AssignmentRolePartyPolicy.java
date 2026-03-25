package org.nmcpye.datarun.party.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.party.dto.CombineMode;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.SoftDeleteObject;
import org.nmcpye.datarun.assignment.assignment.Assignment;

/// @author Hamza Assada 28/12/2025
@Entity
@Table(name = "ASSIGNMENT_ROLE_PARTY_POLICY")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class AssignmentRolePartyPolicy extends JpaIdentifiableObject {

    @Column(name = "uid", length = 11, updatable = false, unique = true, nullable = false)
    protected String uid;

    @Column(name = "code", unique = true)
    protected String code;

    @Column(name = "name", length = 50)
    protected String name;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "defaultPartySet", "createdBy", "createdDate",
            "lastModifiedDate", "lastModifiedBy" }, allowSetters = true)
    @JsonSerialize(contentAs = SoftDeleteObject.class)
    private Assignment assignment;

    /// Precedence Logic: The AssignmentRolePartyPolicy entity correctly allows
    /// vocabularyId to be nullable,
    /// enabling the "Global Role" vs "Specific Form Role" logic.
    /// Adaptability: We are using vocabularyId in the DB, but we use DataTemplate
    /// in our Services/Mappers
    /// to keep it consistent with our existing code.
    /// @see DataTemplate
    @ManyToOne
    @JsonSerialize(contentAs = SoftDeleteObject.class)
    private DataTemplate vocabulary;

    @ManyToOne(optional = false)
    @NotNull
    private PartySet partySet;

    @Column(name = "role", length = 64, nullable = false)
    private String role;

    @Column(name = "combine_mode", length = 32)
    private CombineMode combineMode;

}

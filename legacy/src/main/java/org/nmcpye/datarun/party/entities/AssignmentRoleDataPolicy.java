package org.nmcpye.datarun.party.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.template.datatemplate.DataTemplate;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;
import org.nmcpye.datarun.sharedkernal.SoftDeleteObject;
import org.nmcpye.datarun.sharedkernal.enumeration.AccessLevel;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

/// Control which `data_template` (vocabulary) a *principal* (user/team/group or a `member role`) can see/use inside
/// a specific assignment. e.g. preventing HF users from seeing `Issue` while letting MU officers see it — without hacks.
///
/// @author Hamza Assada 06/01/2026
@Entity
@Table(name = "assignment_role_data_policy")
@EntityListeners(AuditingEntityListener.class)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRoleDataPolicy extends JpaIdentifiableObject {
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
    @ManyToOne
    @JoinColumn(name = "data_template_id", nullable = false)
    @JsonSerialize(contentAs = SoftDeleteObject.class)
    private DataTemplate dataTemplate;

    @Column(name = "role", length = 64, nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 32, nullable = false)
    private AccessLevel accessLevel;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AssignmentRoleDataPolicy that = (AssignmentRoleDataPolicy) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}

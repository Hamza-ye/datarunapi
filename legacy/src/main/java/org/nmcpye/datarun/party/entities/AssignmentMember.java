package org.nmcpye.datarun.party.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Objects;

import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObject;

/**
 * @author Hamza Assada 29/12/2025
 */
@Entity
@Table(name = "assignment_member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentMember extends JpaIdentifiableObject {

    @Column(name = "uid", length = 11, updatable = false, unique = true, nullable = false)
    protected String uid;

    @Column(name = "code", unique = true)
    protected String code;

    @Column(name = "name")
    protected String name;

    @Column(name = "assignment_id", length = 26, nullable = false)
    private String assignmentId;

    @Column(name = "member_type", nullable = false, length = 50)
    private String memberType; // USER | TEAM | USER_GROUP

    @Column(name = "member_id", length = 26, nullable = false)
    private String memberId;

    @Column(name = "role")
    private String role;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        AssignmentMember that = (AssignmentMember) o;
        return Objects.equals(assignmentId, that.assignmentId)
                && Objects.equals(memberType, that.memberType)
                && Objects.equals(memberId, that.memberId)
                && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId, memberType, memberId, role);
    }
}

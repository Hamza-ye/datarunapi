package org.nmcpye.datarun.party.dto;

import lombok.Builder;
import lombok.Data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentMemberDto {
    private String id;
    private String assignmentId;
    private String memberType; // USER | TEAM | USER_GROUP
    private String memberId;
    private String role; // Optional role for the member within the assignment
}

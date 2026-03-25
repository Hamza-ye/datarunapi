package org.nmcpye.datarun.party.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

@Data
@AllArgsConstructor
public class AssignmentManifestProjection {
    String assignmentId;
    String assignmentUid;
    String label;
    String activityUid;
    String orgUnitUid;
    String teamUid;
    Boolean deleted;
    Integer startDay;
    FlowStatus status;
    Set<String> forms;
    Instant lastModifiedDate;

    public AssignmentManifestProjection() {
    }

    // public AssignmentManifestProjection(@NotNull @Size(max = 26) String
    // assignmentId, @NotNull @Size(max = 11) String assignmentUid,
    // String name, @NotNull @Size(max = 11) String activityUid,
    // @NotNull @Size(max = 11) String orgUnitUid, @NotNull @Size(max = 11) String
    // teamUid,
    // Boolean deleted, Integer startDay,
    // String status, List<String> forms,
    // Instant lastModifiedDate) {
    // this.assignmentId = assignmentId;
    // this.assignmentUid = assignmentUid;
    // this.label = name;
    // this.activityUid = activityUid;
    // this.orgUnitUid = orgUnitUid;
    // this.teamUid = teamUid;
    // this.deleted = deleted;
    // this.startDay = startDay;
    // this.status = FlowStatus.valueOf(status);
    // this.forms = forms;
    // this.lastModifiedDate = lastModifiedDate;
    // }
}

package org.nmcpye.datarun.party.dto;

import org.nmcpye.datarun.sharedkernal.enumeration.FlowStatus;

public enum AssignmentStatus {
    PENDING, // SCHEDULED
    IN_PROGRESS,
    DONE,
    RESCHEDULED,
    MERGED,
    REASSIGNED,
    CANCELLED,
    EXPIRED;

    public static AssignmentStatus getAssignmentStatus(FlowStatus flowStatus) {
        if (flowStatus == FlowStatus.PLANNED) return AssignmentStatus.PENDING;

        for (AssignmentStatus status : AssignmentStatus.values()) {
            if (status.name().equalsIgnoreCase(flowStatus.name())) {
                return status;
            }
        }

        return null;
    }

    public static FlowStatus getAssignmentStatus(AssignmentStatus assignmentStatus) {
        if (assignmentStatus == AssignmentStatus.PENDING) return FlowStatus.PLANNED;

        for (FlowStatus status : FlowStatus.values()) {
            if (status.name().equalsIgnoreCase(assignmentStatus.name())) {
                return status;
            }
        }

        return null;
    }
}

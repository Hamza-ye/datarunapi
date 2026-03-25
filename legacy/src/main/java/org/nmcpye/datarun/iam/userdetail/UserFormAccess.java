package org.nmcpye.datarun.iam.userdetail;

import lombok.Builder;
import lombok.Value;

import static org.nmcpye.datarun.sharedkernal.enumeration.FormPermission.*;

import java.time.Instant;
import java.util.Set;

import org.nmcpye.datarun.sharedkernal.enumeration.FormPermission;

/**
 * @author Hamza Assada 24/04/2025 (7amza.it@gmail.com)
 */
@Value
@Builder
public class UserFormAccess {
    String user;
    String team;
    String form;
    Set<FormPermission> permissions;
    Instant validFrom;
    Instant validTo;

    public boolean canViewSubmission() {
        return permissions.stream()
            .anyMatch(FormPermission::canViewSubmission);
    }

    public boolean canViewSubmissionFromUsers() {
        return permissions.contains(VIEW_SUBMISSIONS_FROM_USERS);
    }

    public boolean canAddSubmission() {
        return permissions.contains(ADD_SUBMISSIONS);
    }

    public boolean canEditSubmission() {
        return permissions.contains(EDIT_SUBMISSIONS);

    }

    public boolean canEditSubmissionFromUsers() {
        return permissions.contains(EDIT_SUBMISSIONS_FROM_USERS);
    }

    public boolean canApproveSubmission() {
        return permissions.contains(APPROVE_SUBMISSIONS);
    }

    public boolean canDeleteSubmission() {
        return permissions.contains(DELETE_SUBMISSIONS);
    }

    public boolean canDeleteSubmissionFromUsers(String form) {
        return permissions.contains(DELETE_SUBMISSIONS_FROM_USERS);
    }
}


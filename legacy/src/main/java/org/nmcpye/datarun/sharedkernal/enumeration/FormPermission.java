package org.nmcpye.datarun.sharedkernal.enumeration;

import java.util.Arrays;
import java.util.List;

public enum FormPermission {
    /**
     * the User can view own submissions
     */
    VIEW_SUBMISSIONS,
    /**
     * the User can view other users' submissions
     */
    VIEW_SUBMISSIONS_FROM_USERS,
    /**
     * the User can add new submissions
     */
    ADD_SUBMISSIONS,
    /**
     * the User edit own submissions after submitting
     */
    EDIT_SUBMISSIONS,
    /**
     * The user can edit other users submissions
     */
    EDIT_SUBMISSIONS_FROM_USERS,
    /**
     * The user can approve other users submissions
     */
    APPROVE_SUBMISSIONS,
    /**
     * The user can delete own submissions after submitting
     */
    DELETE_SUBMISSIONS,
    /**
     * The user can Delete other users submissions
     */
    DELETE_SUBMISSIONS_FROM_USERS;

    public static FormPermission[] canViewPermissions() {
        return List.of(VIEW_SUBMISSIONS, VIEW_SUBMISSIONS_FROM_USERS, EDIT_SUBMISSIONS,
            EDIT_SUBMISSIONS_FROM_USERS, APPROVE_SUBMISSIONS, DELETE_SUBMISSIONS,
            DELETE_SUBMISSIONS_FROM_USERS).toArray(new FormPermission[0]);
    }

    public boolean canViewSubmission() {
        return Arrays.stream(canViewPermissions()).toList().contains(this);
    }
}

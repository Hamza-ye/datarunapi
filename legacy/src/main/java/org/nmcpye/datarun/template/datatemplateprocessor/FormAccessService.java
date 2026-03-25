package org.nmcpye.datarun.template.datatemplateprocessor;

import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Named;
import org.nmcpye.datarun.assignment.assignment.Assignment;
import org.nmcpye.datarun.assignment.assignment.dto.AssignmentFormDto;
import org.nmcpye.datarun.iam.security.SecurityUtils;
import org.nmcpye.datarun.sharedkernal.enumeration.FormPermission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.nmcpye.datarun.sharedkernal.enumeration.FormPermission.*;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Hamza Assada 25/04/2025 (7amza.it@gmail.com)
 */
@Service
@Transactional(readOnly = true)
public class FormAccessService {
    public boolean hasAnyOfPermissions(String formUid, FormPermission... requiredPermission) {
        if (!SecurityUtils.isAuthenticated()) {
            return false;
        }
        final var currentUser = SecurityUtils.getCurrentUserDetailsOrThrow();
        if (currentUser.isSuper()) {
            return true;
        }

        return currentUser.getFormAccess().stream()
            .filter(ufa -> ufa.getForm().equals(formUid))
            .anyMatch(ufa -> CollectionUtils.containsAny(ufa.getPermissions(), requiredPermission) &&
                (ufa.getValidFrom() == null || ufa.getValidFrom().isBefore(Instant.now())) &&
                (ufa.getValidTo() == null || ufa.getValidTo().isAfter(Instant.now()))
            );
    }

    public boolean canSubmitData(String formUid) {
        return hasAnyOfPermissions(formUid, ADD_SUBMISSIONS, EDIT_SUBMISSIONS);
    }

    public boolean canEditSubmissions(String form) {
        return hasAnyOfPermissions(form, EDIT_SUBMISSIONS);
    }

    public boolean canAddSubmissions(String form) {
        return hasAnyOfPermissions(form, ADD_SUBMISSIONS);
    }

    public boolean canDeleteSubmissions(String form) {
        return hasAnyOfPermissions(form, DELETE_SUBMISSIONS);
    }

    @Named("assignmentUserForms")
    public Set<AssignmentFormDto> getUserForms(Assignment assignment) {
        if (!SecurityUtils.isAuthenticated()) {
            return Set.of();
        }
        final var currentUser = SecurityUtils.getCurrentUserDetailsOrThrow();
        if (assignment.getForms() == null) {
            return Set.of();
        }

        return assignment.getForms()
            .stream()
            .filter((form) -> currentUser.getUserFormsUIDs()
                .contains(form))
            .map((form) -> AssignmentFormDto.builder()
                .form(form)
                .assignment(assignment.getUid())
                .canAddSubmissions(canAddSubmissions(form))
                .canEditSubmissions(canEditSubmissions(form))
                .canDeleteSubmissions(canDeleteSubmissions(form))
                .build())
            .collect(Collectors.toSet());
    }
}

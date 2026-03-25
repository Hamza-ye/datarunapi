package org.nmcpye.datarun.acl;

import org.nmcpye.datarun.template.datatemplateprocessor.FormAccessService;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.IdentifiableObject;
import org.springframework.stereotype.Service;

/**
 * @author Hamza Assada
 * @since 16/05/2025
 */
@Service
public class DefaultAclService implements AclService {
    private final FormAccessService formAccessService;

    public DefaultAclService(FormAccessService formAccessService) {
        this.formAccessService = formAccessService;
    }

    @Override
    public boolean canRead(IdentifiableObject<?> object, CurrentUserDetails userDetails) {
        if (userDetails == null) return false;
        if (userDetails.isSuper()) return true;
        if (userDetails.getUserTeamsUIDs().isEmpty() || userDetails.getUserTeamsUIDs().isEmpty()) return false;
        return false;
    }

    @Override
    public boolean hasMinimalRights(CurrentUserDetails userDetails) {
        return userDetails.isSuper() || !userDetails.getUserTeamsUIDs().isEmpty();
    }

    @Override
    public boolean canWrite(IdentifiableObject<?> object, CurrentUserDetails userDetails) {
        if (userDetails.isSuper()) return true;
        if (object instanceof DataSubmission submission) {
            return formAccessService.canSubmitData(submission.getForm());
        }
        return false;
    }

    @Override
    public boolean canUpdate(IdentifiableObject<?> object, CurrentUserDetails userDetails) {
        if (userDetails.isSuper()) return true;
        if (object instanceof DataSubmission submission) {
            return formAccessService.canEditSubmissions(submission.getForm());
        }
        return false;
    }

    @Override
    public boolean canAddNew(IdentifiableObject<?> object, CurrentUserDetails userDetails) {
        if (userDetails.isSuper()) return true;
        if (object instanceof DataSubmission submission) {
            return formAccessService.canAddSubmissions(submission.getForm());
        }
        return false;
    }

    @Override
    public boolean canDelete(IdentifiableObject<?> object, CurrentUserDetails userDetails) {
        if (userDetails.isSuper()) return true;
        if (object instanceof DataSubmission submission) {
            return formAccessService.canDeleteSubmissions(submission.getForm());
        }
        return false;
    }
}

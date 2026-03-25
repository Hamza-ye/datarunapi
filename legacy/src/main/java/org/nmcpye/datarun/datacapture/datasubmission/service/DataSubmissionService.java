package org.nmcpye.datarun.datacapture.datasubmission.service;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.EntitySaveSummaryVM;
import org.nmcpye.datarun.sharedkernal.JpaIdentifiableObjectService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Service Interface for managing {@link DataSubmission}.
 */
public interface DataSubmissionService
    extends JpaIdentifiableObjectService<DataSubmission> {
    @Transactional
    DataSubmission upsert(DataSubmission entity, CurrentUserDetails user, EntitySaveSummaryVM summary);

    @Transactional
    List<DataSubmission> upsertAll(Collection<DataSubmission> entities,
                                   CurrentUserDetails user, EntitySaveSummaryVM summary);
}

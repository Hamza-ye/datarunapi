package org.nmcpye.datarun.datacapture.datasubmission.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.assignment.assignment.repository.AssignmentRepository;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.template.datatemplateprocessor.FormAccessService;
import org.nmcpye.datarun.iam.security.CurrentUserDetails;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.springframework.stereotype.Component;

/**
 * @author Hamza Assada 20/08/2025 (7amza.it@gmail.com)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionAccessValidator {
    private final FormAccessService formAccessService;
    private final AssignmentRepository assignmentRepository;

    public DataSubmission validateAccess(DataSubmission submission, CurrentUserDetails user) {
        if (!user.isSuper()) {
            if (!formAccessService.canSubmitData(submission.getForm())) {
                log.error("User {} cannot submit data", user.getUsername());
                throw new IllegalQueryException(ErrorCode.E1112, submission.getTeam());
            }
            if (submission.getAssignment() == null) {
                throw new DomainValidationException("Assignment is required");
            }
            final var assignment = assignmentRepository.findByUid(submission.getAssignment())
                .orElseThrow(() -> new DomainValidationException("Assignment is required"));

            final var incomingTeam = assignment.getTeam().getUid();
            if (!user.getUserTeamsUIDs().contains(incomingTeam)) {
                log.error("User {}, with team {} cannot submit data", user.getUsername(), incomingTeam);
                throw new IllegalQueryException(ErrorCode.E4114, submission.getTeam(), submission.getUid());
            }
        }

        return submission;
    }
}

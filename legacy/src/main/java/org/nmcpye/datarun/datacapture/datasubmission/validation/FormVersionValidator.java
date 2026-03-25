package org.nmcpye.datarun.datacapture.datasubmission.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateInstanceDto;
import org.nmcpye.datarun.template.datatemplate.service.DataTemplateInstanceService;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * @author Hamza Assada
 * @since 15/08/2025
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FormVersionValidator implements SubmissionValidator {
    private final DataTemplateInstanceService dataTemplateInstanceService;

    @Override
    @Transactional(readOnly = true)
    public DataSubmission validateAndEnrich(DataSubmission submission) {
        String form = submission.getForm();
        String formVersionUid = submission.getFormVersion();
        Integer versionNum = submission.getVersion();

        if (Objects.isNull(submission.getForm()) || (Objects.isNull(submission.getFormVersion()) &&
            Objects.isNull(submission.getVersion()))) {
            log.error("Form or FormVersion of submission {} is not set in submission", submission.getUid());
            throw new DomainValidationException(ErrorCode.E4112, submission.getUid());
        }
        DataTemplateInstanceDto resolved = null;

        // try resolve by UID first, then by form & number (your existing helper)
        if (formVersionUid != null) {
            resolved = dataTemplateInstanceService.findByTemplateAndVersionUid(form, formVersionUid).orElseThrow(() -> new DomainValidationException(
                "Form/version uid not found: " + form + ":" + formVersionUid));
        } else {
            resolved = dataTemplateInstanceService.findByTemplateAndVersionNo(form, versionNum).orElseThrow(() -> new DomainValidationException(
                "Form/version No not found: " + form + ":" + versionNum));
        }

        // optionally set canonical form template uid if different
        submission.setForm(resolved.getUid());
        // ensure canonical formVersion is set to resolved UID (enrichment)
        submission.setFormVersion(resolved.getVersionUid());
        submission.setVersion(resolved.getVersionNumber());

        return submission;
    }
}

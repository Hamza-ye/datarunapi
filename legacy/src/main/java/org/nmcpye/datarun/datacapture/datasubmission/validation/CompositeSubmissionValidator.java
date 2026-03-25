package org.nmcpye.datarun.datacapture.datasubmission.validation;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hamza Assada
 * @since 15/08/2025
 */
@Component
@RequiredArgsConstructor
public class CompositeSubmissionValidator implements SubmissionValidator {

    private final List<SubmissionValidator> validators;

    @Override
    public DataSubmission validateAndEnrich(DataSubmission submission) {
        DataSubmission current = submission;
        for (SubmissionValidator v : validators) {
            current = v.validateAndEnrich(current);
        }
        return current;
    }
}

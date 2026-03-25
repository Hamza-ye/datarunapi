package org.nmcpye.datarun.datacapture.datasubmission.validation;

import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;

/**
 * @author Hamza Assada
 * @since 15/08/2025
 */
public interface SubmissionValidator {
    /**
     * Validate and possibly enrich the DTO. Throw DomainValidationException on failure.
     * Return the (possibly enriched) submission for fluent chaining.
     */
    DataSubmission validateAndEnrich(DataSubmission submission);
}

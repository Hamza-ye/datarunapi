package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
public class TemplateValidationException extends IllegalQueryException {
    private final TemplateValidationResult result;

    public TemplateValidationException(TemplateValidationResult result) {
        super(new ErrorMessage(ErrorCode.E1111, result.toString()));
        this.result = result;
    }

    public TemplateValidationResult getResult() {
        return result;
    }
}

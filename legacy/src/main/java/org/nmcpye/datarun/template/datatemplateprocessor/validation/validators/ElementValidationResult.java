package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

import java.util.Collections;
import java.util.List;

/**
 * @author Hamza Assada 25/03/2025 (7amza.it@gmail.com)
 */

public class ElementValidationResult {
    private final boolean valid;
    private final List<ElementExpressionValidationError> errors;

    public ElementValidationResult(boolean valid, List<ElementExpressionValidationError> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public List<ElementExpressionValidationError> getErrors() {
        return errors;
    }

    public static ElementValidationResult valid() {
        return new ElementValidationResult(true, Collections.emptyList());
    }

    public static ElementValidationResult invalid(List<ElementExpressionValidationError> errors) {
        return new ElementValidationResult(false, errors);
    }
}

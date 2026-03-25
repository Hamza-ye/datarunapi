package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Hamza Assada 25/03/2025 (7amza.it@gmail.com)
 */

public class TemplateValidationResult {
    private final boolean valid;
    private final List<? extends TemplateValidationError> errors;

    public TemplateValidationResult(boolean valid, List<? extends TemplateValidationError> errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public boolean isValid() {
        return valid;
    }

    public List<?> getErrors() {
        return errors;
    }

    public static TemplateValidationResult valid() {
        return new TemplateValidationResult(true, Collections.emptyList());
    }

    public static TemplateValidationResult invalid(List<? extends TemplateValidationError> errors) {
        return new TemplateValidationResult(false, errors);
    }

    @Override
    public String toString() {
        return "FormValidationResult{" +
            "errors=" + errors.stream().map(Objects::toString).toList() +
            '}';
    }
}

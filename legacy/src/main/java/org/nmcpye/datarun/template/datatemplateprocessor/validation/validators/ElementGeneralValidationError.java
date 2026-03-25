package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
public record ElementGeneralValidationError(
    String message
) implements TemplateValidationError {
    @Override
    public String toString() {
        return """
            Duplicate Validation Error:
              - message: %s
            """.formatted(message);
    }
}

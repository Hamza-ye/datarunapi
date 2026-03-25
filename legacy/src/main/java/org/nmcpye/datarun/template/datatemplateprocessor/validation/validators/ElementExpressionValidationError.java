package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
public record ElementExpressionValidationError(
    String expression,
    String invalidReference,
    String message
) implements TemplateValidationError {
    @Override
    public String toString() {
        return """
            Validation Error:
              - Element: %s
              - Context: %s
              - Message: %s
            """.formatted(expression, invalidReference, message);
    }
}

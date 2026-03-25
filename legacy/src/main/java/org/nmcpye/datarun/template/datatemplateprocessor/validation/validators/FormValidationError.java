package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
public record FormValidationError(
    String sourceElement,
    String contextType,
    TemplateValidationError elementValidationError
) implements TemplateValidationError {
    @Override
    public String toString() {
        return """
            Validation Error:
              - Element: %s
              - Context: %s
              - Element Validation Error: %s
            """.formatted(sourceElement,
            contextType,
            elementValidationError.toString());
    }
}

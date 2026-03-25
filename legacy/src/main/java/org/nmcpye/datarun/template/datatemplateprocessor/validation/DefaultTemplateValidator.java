package org.nmcpye.datarun.template.datatemplateprocessor.validation;

import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.TemplateValidationException;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.TemplateValidator;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
@Component
public class DefaultTemplateValidator {
    final List<? extends TemplateValidator> validators;

    public DefaultTemplateValidator(List<? extends TemplateValidator> validators) {
        this.validators = validators;
    }

    public <T extends DataTemplateVersionInterface> void validate(T formTemplate) {
        for (final var validator : validators) {
            final var result = validator.validate(formTemplate);
            if (!result.isValid()) {
                throw new TemplateValidationException(result);
            }
        }
    }
}

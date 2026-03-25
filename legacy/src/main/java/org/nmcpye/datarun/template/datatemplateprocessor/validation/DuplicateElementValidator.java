package org.nmcpye.datarun.template.datatemplateprocessor.validation;

import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.ElementGeneralValidationError;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.FormValidationError;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.TemplateValidationResult;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.TemplateValidator;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Hamza Assada 19/03/2025 (7amza.it@gmail.com)
 */
@Component
public class DuplicateElementValidator implements TemplateValidator {

    public TemplateValidationResult validate(DataTemplateVersionInterface template) {
        final var sectionsErrors = validateNoDuplicates(
            new ArrayList<>(template.getSections()),
            AbstractElement::getName,
            template.getUid(),
            "section"
        );

        // Validate fields by ID
        final var fieldErrors = validateNoDuplicates(
            template.getFields(),
            FieldTemplateElementDto::getId,
            template.getUid(),
            "field"
        );
        final List<FormValidationError> errors = new ArrayList<>();

        if (!sectionsErrors.isEmpty()) {
            errors.addAll(sectionsErrors);
        }
        if (!fieldErrors.isEmpty()) {
            errors.addAll(fieldErrors);
        }

        if (!errors.isEmpty()) {
            return TemplateValidationResult.invalid(errors);
        }
        return TemplateValidationResult.valid();
    }

    private static <T extends AbstractElement> List<FormValidationError> validateNoDuplicates(
        Collection<? extends T> elements,
        Function<T, String> propertyExtractor,
        String formTemplateUid,
        String elementType
    ) {
        Map<String, List<T>> groupedElements = elements.stream()
            .collect(Collectors.groupingBy(propertyExtractor));

        List<FormValidationError> errors = new ArrayList<>();
        groupedElements.forEach((key, elementsWithKey) -> {
            if (elementsWithKey.size() > 1) {
                List<String> duplicateDetails = elementsWithKey.stream()
                    .map(e -> String.format(
                        "%s (path: %s)",
                        e.getName(),
                        e.getPath()
                    ))
                    .toList();
                errors.add(new FormValidationError(key, String.format(
                    "'%s':%s)",
                    formTemplateUid,
                    elementType
                ), new ElementGeneralValidationError(String.format(
                    "Duplicate : %s",
                    duplicateDetails
                ))));
            }
        });
        return errors;
    }
}

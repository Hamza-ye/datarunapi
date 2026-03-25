package org.nmcpye.datarun.template.datatemplateprocessor.validation.validators;

import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Hamza Assada 25/03/2025 (7amza.it@gmail.com)
 */
public class ElementReferenceValidator implements ExpressionValidator {

    private static final Pattern ELEMENT_REF_PATTERN = Pattern.compile("#\\{(\\w+)}");

    @Override
    public ElementValidationResult validate(String expression, DataTemplateVersionInterface template) {
        List<ElementExpressionValidationError> errors = new ArrayList<>();

        // Extract element references from the expression.
        Matcher matcher = ELEMENT_REF_PATTERN.matcher(expression);
        Set<String> referencedElements = new HashSet<>();
        while (matcher.find()) {
            referencedElements.add(matcher.group(1));
        }

        Set<String> validElementNames = template.getFields().stream()
            .map(AbstractElement::getName)
            .collect(Collectors.toSet());

        // validElementNames.addAll(template.getSections().stream().map(Section::getName).collect(Collectors.toSet()));

        for (String ref : referencedElements) {
            if (!validElementNames.contains(ref)) {
                errors.add(
                    new ElementExpressionValidationError(
                        expression,
                        ref,
                        "Referenced element '%s' does not exist in the form template".formatted(ref)
                    )
                );
            }
        }

        return errors.isEmpty() ? ElementValidationResult.valid() : ElementValidationResult.invalid(errors);
    }
}

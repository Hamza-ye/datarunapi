package org.nmcpye.datarun.template.datatemplateprocessor.validation;

import org.nmcpye.datarun.template.datatemplateelement.DataFieldRule;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;
import org.nmcpye.datarun.template.datatemplateprocessor.validation.validators.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hamza Assada 25/03/2025 (7amza.it@gmail.com)
 */
@Component
public class TemplateExpressionValidator
    implements TemplateValidator {

    private final List<ExpressionValidator> validators = List.of(new ElementReferenceValidator());

    /**
     * Validates all expressions (rules and constraints) within a DataFormTemplate.
     *
     * @param template the template to validate.
     * @return a ValidationResult summarizing all issues.
     */
    @Override
    public TemplateValidationResult validate(DataTemplateVersionInterface template) {
        List<FormValidationError> errors = new ArrayList<>();

        // Validate rule expressions in fields.
        for (FieldTemplateElementDto field : template.getFields()) {
            if (field.getRules() != null) {
                for (DataFieldRule rule : field.getRules()) {
                    String expression = rule.getExpression();
                    for (ExpressionValidator validator : validators) {
                        ElementValidationResult result = validator.validate(expression, template);
                        if (!result.isValid()) {
                            errors.addAll(result.getErrors().stream()
                                .map(err -> new FormValidationError(field.getName(), "rule error", err))
                                .toList());
                            // "Field '" + field.getName() + "' rule error: " + err
                        }
                    }
                }
            }
            // Validate constraint expressions if present.

            String expression = field.getConstraint() != null && !field.getConstraint().isEmpty() ?
                field.getConstraint() : field.getValidationRule() != null ?
                field.getValidationRule().getExpression() : null;
            if (expression != null) {
                for (ExpressionValidator validator : validators) {
                    ElementValidationResult result = validator.validate(expression.trim(), template);
                    if (!result.isValid()) {
                        errors.addAll(result.getErrors().stream()
                            .map(err -> new FormValidationError(field.getName(), "constraint error", err))
                            .toList());
                    }
                }
            }

        }

        // Similarly, validate rules in sections
        for (SectionTemplateElementDto section : template.getSections()) {
            if (section.getRules() != null) {
                for (DataFieldRule rule : section.getRules()) {
                    String expression = rule.getExpression();
                    for (ExpressionValidator validator : validators) {
                        ElementValidationResult result = validator.validate(expression, template);
                        if (!result.isValid()) {
                            errors.addAll(result.getErrors().stream()
                                .map(err -> new FormValidationError(section.getName(), "Section", err))
                                .toList());
                        }
                    }
                }
            }
        }

        return errors.isEmpty() ? TemplateValidationResult.valid() : TemplateValidationResult.invalid(errors);
    }


//    // De implementation
//
//    // dee
//    private static final Pattern ELEMENT_REFERENCE_PATTERN = Pattern.compile("#\\{([^}]+)}");
//    private static final Set<String> RESERVED_KEYWORDS = Set.of("true", "false", "null", "empty");
//
//    public FormValidationResult validateTemplate(DataFormTemplate template) {
//        final Set<String> availableElements = collectAllElementNames(template);
//        final List<FormValidationError> errors = new ArrayList<>();
//
//        // Validate fields' rules and constraints
//        template.getFields().forEach(field -> {
//            validateExpressions(field.getRules(), availableElements, errors, field.getName(), "Field");
//            if (StringUtils.hasText(field.getConstraint())) {
//                validateExpression(field.getConstraint(), availableElements, errors,
//                    field.getName(), "Field constraint");
//            }
//        });
//
//        // Validate sections' rules
//        template.getSections().forEach(section -> {
//            validateExpressions(section.getRules(), availableElements, errors,
//                section.getName(), "Section");
//        });
//
//        if (!errors.isEmpty()) {
//            return FormValidationResult.invalid(errors);
//        }
//        return FormValidationResult.valid();
//    }
//
//    private Set<String> collectAllElementNames(DataFormTemplate template) {
//        return Stream.concat(
//            template.getFields().stream().map(FormDataElementConf::getName),
//            template.getSections().stream().map(FormSectionConf::getName)
//        ).collect(Collectors.toSet());
//    }
//
//    /**
//     * @param rules         element's rules
//     * @param elements      available elements in the form
//     * @param errors        errors
//     * @param sourceElement the rule's owner element's name
//     * @param elementType   context, e.g Field, Section, Constraint
//     */
//    private void validateExpressions(List<DataFieldRule> rules, Set<String> elements,
//                                     List<FormValidationError> errors,
//                                     String sourceElement, String elementType) {
//        rules.forEach(rule ->
//            validateExpression(rule.getExpression(), elements, errors,
//                sourceElement, elementType + " rule")
//        );
//    }
//
//    private void validateExpression(String expression, Set<String> elements,
//                                    List<FormValidationError> errors,
//                                    String sourceElement, String context) {
//        if (!StringUtils.hasText(expression)) return;
//
//        getReferencedElements(expression).forEach(referenced -> {
//            if (!elements.contains(referenced) && !RESERVED_KEYWORDS.contains(referenced)) {
//                errors.add(new FormValidationError(
//                    sourceElement,
//                    context,
//                    new ElementExpressionValidationError(expression,
//                        referenced,
//                        "Referenced element '%s' does not exist in the form template".formatted(referenced))
//                ));
//            }
//        });
//    }
//
//    private Set<String> getReferencedElements(String expression) {
//        final Set<String> references = new HashSet<>();
//        final Matcher matcher = ELEMENT_REFERENCE_PATTERN.matcher(expression);
//
//        while (matcher.find()) {
//            final String reference = matcher.group(1).trim();
//            if (!reference.isEmpty()) {
//                references.add(reference);
//            }
//        }
//        return references;
//    }
}

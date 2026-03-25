package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.ElementValidationRule;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.RuleAction;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.sharedkernal.enumeration.ValueTypeRendering;

import java.util.Optional;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public class MigrateDeprecatedPropertiesHandler
    extends AbstractTemplateElementHandler<FieldTemplateElementDto> {
    private final DataElement source;

    public MigrateDeprecatedPropertiesHandler(DataElement source) {
        this.source = source;
    }

    @Override
    protected FieldTemplateElementDto handle(FieldTemplateElementDto element) {
        final var errorRule = element.getRules().stream()
            .filter(r -> r.getAction() == RuleAction.Error)
            .findAny();
        if (element.getConstraint() == null && errorRule.isPresent()) {
            final var elementValidationRule = new ElementValidationRule()
                .setExpression(errorRule.get().getExpression())
                .setValidationMessage(errorRule.get().getMessage());
            element.setValidationRule(elementValidationRule);
        }

        if (source.getValueType() == ValueType.ScannedCode) {
            element
                .setValueTypeRendering(Optional.ofNullable(element.getValueTypeRendering())
                    .orElse(ValueTypeRendering.BAR_CODE));

            if (element.getGs1Enabled() == Boolean.TRUE) {
                element.setValueTypeRendering(Optional.ofNullable(element.getValueTypeRendering())
                    .orElse(ValueTypeRendering.GS1_DATAMATRIX));
            }
        }

        return element;
    }
}

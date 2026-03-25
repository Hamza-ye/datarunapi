package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.ValueType;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public class ValidateValueTypeHandler
    extends AbstractTemplateElementHandler<FieldTemplateElementDto> {

    private final DataElement source;

    public ValidateValueTypeHandler(DataElement source) {
        this.source = source;
    }

    @Override
    protected FieldTemplateElementDto handle(FieldTemplateElementDto element) {
        ValueType declared = element.getType();
        ValueType actual   = source.getValueType();

        if (declared != null && !declared.isCompatible(actual)) {
            // E1105: element ID, declared type, actual type
            throw new IllegalQueryException(
                new ErrorMessage(
                    ErrorCode.E1105,
                    element.getId(),
                    declared,
                    actual
                )
            );
        }

        // ensure the element ends up with the compatible type
        return element.type(declared);
    }
}

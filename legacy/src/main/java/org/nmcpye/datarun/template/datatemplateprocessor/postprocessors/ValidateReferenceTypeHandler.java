package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public class ValidateReferenceTypeHandler
    extends AbstractTemplateElementHandler<FieldTemplateElementDto> {
    private final DataElement source;

    public ValidateReferenceTypeHandler(DataElement source) {
        this.source = source;
    }

    @Override
    protected FieldTemplateElementDto handle(FieldTemplateElementDto element) {
        if (source.getValueType().isReference() && element.getResourceMetadataSchema() == null) {
            throw new IllegalQueryException(new ErrorMessage(ErrorCode.E1103, element.getParent(), element.getName()));
        }
        return element;
    }
}

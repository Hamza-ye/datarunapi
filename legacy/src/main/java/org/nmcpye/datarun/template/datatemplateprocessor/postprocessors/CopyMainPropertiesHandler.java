package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.dataelement.DataElement;

import java.util.Optional;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public class CopyMainPropertiesHandler
    extends AbstractTemplateElementHandler<FieldTemplateElementDto> {
    private final DataElement source;

    public CopyMainPropertiesHandler(DataElement source) {
        this.source = source;
    }

    @Override
    protected FieldTemplateElementDto handle(FieldTemplateElementDto element) {
        element.setId(source.getUid());
        element.setCode(source.getCode());
        element.setType(source.getValueType());
        element.setName(source.getName());
        element.setDescription(Optional.ofNullable(element.getDescription()).orElse(source.getDescription()));
        element.setLabel(Optional.ofNullable(element.getLabel()).orElse(source.getLabel()));
        return element;
    }
}

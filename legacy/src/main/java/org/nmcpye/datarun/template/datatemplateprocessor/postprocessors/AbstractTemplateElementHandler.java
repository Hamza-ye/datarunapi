package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.springframework.lang.Nullable;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public abstract class AbstractTemplateElementHandler<T extends AbstractElement>
    implements TemplateElementHandler<T> {

    private TemplateElementHandler<T> next;

    public static FieldTemplateElementDto processElement(FieldTemplateElementDto element, @Nullable DataElement source) {

        TemplateElementHandler<FieldTemplateElementDto> handlerChain = new ValidateValueTypeHandler(source);
        handlerChain
            .linkWith(new CopyMainPropertiesHandler(source))
            .linkWith(new ValidateReferenceTypeHandler(source))
            .linkWith(new MigrateDeprecatedPropertiesHandler(source));

        return handlerChain.process(element);
    }

    public static SectionTemplateElementDto processSection(SectionTemplateElementDto element) {

        TemplateElementHandler<SectionTemplateElementDto> handlerChain = new ValidateSectionNameNotNull();
        return handlerChain.process(element);
    }

    @Override
    public TemplateElementHandler<T> linkWith(TemplateElementHandler<T> next) {
        this.next = next;
        return next;
    }

    @Override
    public T process(T element) {
        T processedElement = handle(element);
        if (next != null) {
            return next.process(processedElement);
        }
        return processedElement;
    }

    protected abstract T handle(T element);
}

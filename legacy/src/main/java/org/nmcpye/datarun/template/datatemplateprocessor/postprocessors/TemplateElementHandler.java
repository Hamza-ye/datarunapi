package org.nmcpye.datarun.template.datatemplateprocessor.postprocessors;

import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;

/**
 * @author Hamza Assada 18/03/2025 (7amza.it@gmail.com)
 */
public interface TemplateElementHandler<T extends AbstractElement> {
    TemplateElementHandler<T> linkWith(TemplateElementHandler<T> next);

    T process(T element);
}

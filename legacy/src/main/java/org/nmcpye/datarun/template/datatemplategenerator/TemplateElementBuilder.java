package org.nmcpye.datarun.template.datatemplategenerator;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;

/**
 * Convert field/section snapshots + metadata to
 * TemplateElement entities (in-memory only).
 *
 * @author Hamza Assada
 * @since 09/09/2025
 */
public interface TemplateElementBuilder {
    TemplateElement buildTemplateElementFromField(FieldTemplateElementDto field,
                                                  PathMetadata meta,
                                                  TemplateVersion templateVersion);

    TemplateElement buildTemplateElementFromRepeat(SectionTemplateElementDto section, PathMetadata meta,
                                                   TemplateVersion templateVersion);
}


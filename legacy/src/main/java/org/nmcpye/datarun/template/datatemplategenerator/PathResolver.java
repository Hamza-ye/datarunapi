package org.nmcpye.datarun.template.datatemplategenerator;


import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;

/**
 * Resolve path metadata for a field given section definitions map.
 *
 * @author Hamza Assada
 * @since 09/09/2025
 */
public interface PathResolver {
    PathMetadata resolveForField(FieldTemplateElementDto field);
    PathMetadata resolveForSection(SectionTemplateElementDto section);
}

package org.nmcpye.datarun.template.datatemplate.dto;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;

import java.util.List;

/**
 * @author Hamza Assada 26/03/2025 (7amza.it@gmail.com)
 */
public interface DataTemplateVersionInterface {
    String getUid();

    List<FieldTemplateElementDto> getFields();

    List<SectionTemplateElementDto> getSections();

    DataTemplateVersionInterface sections(List<SectionTemplateElementDto> sections);

    DataTemplateVersionInterface fields(List<FieldTemplateElementDto> fields);
}

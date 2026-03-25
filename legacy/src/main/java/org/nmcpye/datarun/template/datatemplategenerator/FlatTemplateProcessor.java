package org.nmcpye.datarun.template.datatemplategenerator;

import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplate.TemplateVersion;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Produce a small, immutable snapshot that's easy to consume by the generator.
 *
 * @author Hamza Assada
 * @since 09/09/2025
 */
@Component
public class FlatTemplateProcessor {

    public static class TemplateFlatSnapshot {
        public final Map<String, SectionTemplateElementDto> sectionByName;
        public final List<FieldTemplateElementDto> fields;

        public TemplateFlatSnapshot(Map<String, SectionTemplateElementDto> sectionByName,
                                    List<FieldTemplateElementDto> fields) {
            this.sectionByName = Collections.unmodifiableMap(sectionByName);
            this.fields = Collections.unmodifiableList(fields);
        }
    }

    public TemplateFlatSnapshot process(TemplateVersion dtv) {
        Objects.requireNonNull(dtv, "DataTemplateVersion required");
        List<SectionTemplateElementDto> sections = Optional.ofNullable(dtv.getSections()).orElse(Collections.emptyList());
        Map<String, SectionTemplateElementDto> sectionByName = sections.stream()
            .filter(s -> s.getName() != null && !s.getName().isEmpty())
            .collect(Collectors.toMap(SectionTemplateElementDto::getName, s -> s, (a, b) -> a, LinkedHashMap::new));

        List<FieldTemplateElementDto> fields = Optional.ofNullable(dtv.getFields()).orElse(Collections.emptyList());

        return new TemplateFlatSnapshot(sectionByName, fields);
    }
}

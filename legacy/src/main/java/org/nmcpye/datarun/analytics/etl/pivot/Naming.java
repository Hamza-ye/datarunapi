package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public final class Naming {
    private final DataTemplateRepository templateRepository;

    /**
     * Sanitize a template or activity id into a safe token for table names.
     * Keeps only alphanumeric chars and replaces others with underscore, lowercased.
     */
    public static String sanitize(String s) {
        if (s == null) return "null";
        return s.replaceAll("[^A-Za-z0-9]", "_").toLowerCase();
    }

    /**
     * Base facts table name for a template, no schema prefix.
     * Example: template "Senate-2025" -> "fact_senate_2025"
     */
    public String factsBaseForTemplate(String templateUid) {
        return "fact_" + sanitize(templateUid);
    }

    /**
     * Full qualified table name including schema.
     * Example: "analytics.fact_senate_2025"
     */
    public String fqFactTableForTemplate(String templateUid) {
        final var template = templateRepository.findByUid(templateUid).orElseThrow();
        final String code = template.getCode();
        return "pivot." + factsBaseForTemplate(code != null ? code : templateUid);
    }

    public static String newName(String baseFqName) {
        return baseFqName + "_new";
    }

    public static String oldName(String baseFqName) {
        return baseFqName + "_old";
    }

    public static String repeatsTable(String baseFq) {
        return baseFq + "_repeats";
    }
}

package org.nmcpye.datarun.analytics.etl.pivot;


import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CeMetadataRepository {
    private final JdbcTemplate jdbc;

    // New: fetch CEs for a specific template_uid
    public List<CanonicalElement> getElementsForTemplate(String templateUid) {
        String sql = "SELECT id, safe_name, data_type, semantic_type, parent_repeat_id "
            + "FROM public.canonical_element WHERE template_uid = ? ORDER BY safe_name";
        List<CanonicalElement> ces = jdbc.query(sql, new Object[]{templateUid}, CanonicalElement.MAPPER);
        return ces;
    }

    //---------------------------
    public List<CanonicalElementWithConfig> getElementsForTemplateWithConfig(String templateUid) {
        String sql = """
            SELECT
              ce.id,
              ce.template_uid,
              ce.safe_name,
              ce.data_type,
              ce.semantic_type,
              ce.parent_repeat_id,
              COALESCE (cec.safe_name_override, ce.safe_name) AS safe_name_override,
              COALESCE (cec.explode, FALSE) AS explode
            FROM
              PUBLIC.canonical_element ce
              LEFT JOIN analytics.canonical_element_config cec ON cec.canonical_element_id = ce.id
            WHERE
              ce.template_uid = ?
              AND (cec.disabled IS NULL OR cec.disabled IS FALSE)
            ORDER BY
              COALESCE (cec.safe_name, ce.safe_name)
            """;

        return jdbc.query(sql, new Object[]{templateUid}, CanonicalElementWithConfig.MAPPER);
    }
}

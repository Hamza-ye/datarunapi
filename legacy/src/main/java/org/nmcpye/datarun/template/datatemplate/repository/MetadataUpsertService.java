package org.nmcpye.datarun.template.datatemplate.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.CanonicalElement;
import org.nmcpye.datarun.template.datatemplate.TemplateElement;
import org.nmcpye.datarun.analytics.util.UuidUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetadataUpsertService {
    private final NamedParameterJdbcTemplate npJdbc;
    private final ObjectMapper objectMapper;

    private static final int BATCH_SIZE = 800; // tune for your env

    // -------- canonical_element upsert ----------
    private static final String CANONICAL_UPSERT_SQL = """
        INSERT INTO canonical_element
            (id, template_uid, preferred_name, safe_name, data_type, semantic_type, display_label,
             canonical_path, json_data_paths, option_set_uid, option_set_id, parent_repeat_id, created_date, last_modified_date)
          VALUES
            (:id, :templateUid, :preferredName, :safeName, :dataType, :semanticType, CAST(:displayLabel AS jsonb),
             :canonicalPath, CAST(:jsonDataPaths AS jsonb), :optionSetUid, :optionSetId, :parentRepeatUid, now(), now())
          ON CONFLICT (id) DO UPDATE
            SET preferred_name = EXCLUDED.preferred_name,
                data_type = EXCLUDED.data_type,
                semantic_type = EXCLUDED.semantic_type,
                canonical_path = COALESCE(canonical_element.canonical_path, EXCLUDED.canonical_path),
                safe_name = COALESCE(canonical_element.safe_name, EXCLUDED.safe_name),
                parent_repeat_id = COALESCE(canonical_element.parent_repeat_id, EXCLUDED.parent_repeat_id),
                option_set_uid = COALESCE(canonical_element.option_set_uid, EXCLUDED.option_set_uid),
                option_set_id = COALESCE(canonical_element.option_set_id, EXCLUDED.option_set_id),
                display_label = canonical_element.display_label || EXCLUDED.display_label,
                json_data_paths = (
                   SELECT jsonb_agg(DISTINCT elem)
                   FROM jsonb_array_elements_text(
                      COALESCE(canonical_element.json_data_paths, '[]'::jsonb) || COALESCE(EXCLUDED.json_data_paths, '[]'::jsonb)
                   ) AS t(elem)
                ),
                last_modified_date = EXCLUDED.last_modified_date;
        """;

    @Transactional
    public void upsertCanonicalElements(List<CanonicalElement> elems) {
        if (elems == null || elems.isEmpty()) return;
        List<SqlParameterSource> batch = new ArrayList<>(elems.size());
        for (CanonicalElement e : elems) {
            MapSqlParameterSource p = new MapSqlParameterSource();
            var ceId = UuidUtils.toUuidOrNull(e.getId());
            p.addValue("id", ceId, Types.OTHER);
            p.addValue("templateUid", e.getTemplateUid());
            p.addValue("preferredName", e.getPreferredName());
            p.addValue("dataType", e.getDataType() == null ? null : e.getDataType().name());
            p.addValue("semanticType", e.getSemanticType() == null ? null : e.getSemanticType().name());
            try {
                p.addValue("displayLabel", objectMapper.writeValueAsString(e.getDisplayLabel()));
                p.addValue("jsonDataPaths", objectMapper.writeValueAsString(e.getJsonDataPaths()));
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
            p.addValue("canonicalPath", e.getCanonicalPath()); // canonicalPath may be a string/list; objectMapper ensures JSON
            p.addValue("safeName", e.getSafeName());
            p.addValue("parentRepeatUid", e.getParentRepeatId());
            p.addValue("optionSetUid", e.getOptionSetUid());
            p.addValue("optionSetId", e.getOptionSetId());
            batch.add(p);
        }
        // chunked execution
        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            int end = Math.min(batch.size(), i + BATCH_SIZE);
            List<SqlParameterSource> slice = batch.subList(i, end);
            npJdbc.batchUpdate(CANONICAL_UPSERT_SQL, slice.toArray(new SqlParameterSource[0]));
        }
    }

    // -------- template_element upsert ----------
    private static final String TEMPLATE_UPSERT_SQL = """
        INSERT INTO template_element (uid, template_uid, template_version_uid, template_version_no,
                                      canonical_element_id, json_data_path, canonical_path, name, data_element_uid,
           data_type, semantic_type, option_set_uid, option_set_id, parent_repeat_json_data_path,
           parent_repeat_canonical_path, display_label, sort_order, created_date)
        VALUES
          (:uid, :templateUid, :templateVersionUid, :templateVersionNo, :canonicalElementId,
           :jsonDataPath, :canonicalPath, :name, :dataElementUid,
           :dataType, :semanticType, :optionSetUid, :optionSetId, :parentRepeatJsonDataPath,
           :parentRepeatCanonicalPath, CAST(:displayLabel AS jsonb), :sortOrder, now())
         ON CONFLICT (template_uid, template_version_uid, canonical_path) DO Nothing;
        """;

    @Transactional
    public void upsertTemplateElements(List<TemplateElement> elems) {
        if (elems == null || elems.isEmpty()) return;
        List<SqlParameterSource> batch = new ArrayList<>(elems.size());
        for (TemplateElement e : elems) {
            MapSqlParameterSource p = new MapSqlParameterSource();
            p.addValue("uid", e.getUid());
            p.addValue("templateUid", e.getTemplateUid());
            p.addValue("templateVersionUid", e.getTemplateVersionUid());
            p.addValue("templateVersionNo", e.getTemplateVersionNo());
            var ceId = UuidUtils.toUuidOrNull(e.getCanonicalElementId());
            p.addValue("canonicalElementId", ceId);
            p.addValue("jsonDataPath", e.getJsonDataPath());
            p.addValue("canonicalPath", e.getCanonicalPath());
            p.addValue("name", e.getName());
            p.addValue("dataElementUid", e.getDataElementUid());
            p.addValue("dataType", e.getDataType() == null ? null : e.getDataType().name());
            p.addValue("semanticType", e.getSemanticType() == null ? null : e.getSemanticType().name());
            p.addValue("optionSetUid", e.getOptionSetUid());
            p.addValue("optionSetId", e.getOptionSetId());
            p.addValue("parentRepeatJsonDataPath", e.getParentRepeatJsonDataPath());
            p.addValue("parentRepeatCanonicalPath", e.getParentRepeatCanonicalPath());
            try {
                p.addValue("displayLabel", objectMapper.writeValueAsString(e.getDisplayLabel()));
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
            p.addValue("sortOrder", e.getSortOrder());
            batch.add(p);
        }

        for (int i = 0; i < batch.size(); i += BATCH_SIZE) {
            int end = Math.min(batch.size(), i + BATCH_SIZE);
            List<SqlParameterSource> slice = batch.subList(i, end);
            npJdbc.batchUpdate(TEMPLATE_UPSERT_SQL, slice.toArray(new SqlParameterSource[0]));
        }
    }
}

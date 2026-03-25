package org.nmcpye.datarun.analytics.etl.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.analytics.etl.dto.TallCanonicalValue;
import org.nmcpye.datarun.analytics.etl.model.SubmissionContext;
import org.nmcpye.datarun.analytics.etl.util.InstanceKeyUtil;
import org.nmcpye.datarun.analytics.util.UuidUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("ConcatenationWithEmptyString")
@Repository
@RequiredArgsConstructor
@Slf4j
public class TallCanonicalJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    final String sql = ""
        + "INSERT INTO analytics.tall_canonical ("
        + "  instance_key, activity_uid, assignment_uid, org_unit_uid, team_uid,  canonical_element_id, outbox_id, ingest_id,"
        + "  submission_id, submission_uid, submission_serial_number, template_uid, template_version_uid,"
        + "  element_path, repeat_instance_id, parent_instance_id, repeat_index,"
        + "  value_text, value_bool, value_number, value_json, value_ref_type, value_ref_uid, submission_creation_time, start_time, created_at, updated_at"
        + ") VALUES ("
        + "  :instance_key, :activity_uid, :assignment_uid, :org_unit_uid, :team_uid, :canonical_element_id, :outbox_id, :ingest_id,"
        + "  :submission_id, :submission_uid, :submission_serial_number, :template_uid, :template_version_uid,"
        + "  :element_path, :repeat_instance_id, :parent_instance_id, :repeat_index,"
        + "  :value_text, :value_bool, :value_number, cast(:value_json AS jsonb), :value_ref_type, :value_ref_uid, "
        + ":submission_creation_time, :start_time, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP"
        + ") "
        + "ON CONFLICT (instance_key, canonical_element_id) DO UPDATE SET "
        + "  activity_uid = EXCLUDED.activity_uid, "
        + "  assignment_uid = EXCLUDED.assignment_uid, "
        + "  org_unit_uid = EXCLUDED.org_unit_uid, "
        + "  team_uid = EXCLUDED.team_uid, "
        + "  value_text = EXCLUDED.value_text, "
        + "  value_bool = EXCLUDED.value_bool, "
        + "  value_number = EXCLUDED.value_number, "
        + "  value_json = EXCLUDED.value_json, "
        + "  value_ref_type = EXCLUDED.value_ref_type, "
        + "  value_ref_uid = EXCLUDED.value_ref_uid, "
        + "  outbox_id = EXCLUDED.outbox_id, "
        + "  ingest_id = EXCLUDED.ingest_id, "
        + "  submission_creation_time = coalesce(EXCLUDED.submission_creation_time, tall_canonical.submission_creation_time),"
        + "  start_time = coalesce(EXCLUDED.start_time, tall_canonical.start_time),"
        + "  updated_at = CURRENT_TIMESTAMP; ";

    // truncation thresholds
    private static final int VALUE_TEXT_MAX = 4000;
    private static final int VALUE_JSON_MAX = 10000;

    /**
     * Upsert a mixed batch of TallCanonicalRow entries.
     * <p>
     * - Calculates instance_key = COALESCE(repeat_instance_id, submission_uid) and binds it explicitly.
     * - Sanitizes/truncates value_text and value_json to reasonable sizes to avoid extremely large payloads.
     * - Executes a parameterized batchUpdate using MapSqlParameterSource[] for performance.
     * <p>
     * On DB errors this method throws DataAccessException so callers can handle retries/failure.
     */
    public void upsertBatch(SubmissionContext context, List<TallCanonicalValue> rows) {
        if (rows == null || rows.isEmpty()) return;

        MapSqlParameterSource[] params = new MapSqlParameterSource[rows.size()];
        int i = 0;
        for (var r : rows) {
            String instanceKey = InstanceKeyUtil.computeInstanceKey(context.submissionUid(), r);
            MapSqlParameterSource p = new MapSqlParameterSource();
            p.addValue("instance_key", instanceKey);
            p.addValue("activity_uid", context.activityUid());
            p.addValue("org_unit_uid", context.orgUnitUid());
            p.addValue("team_uid", context.teamUid());
            p.addValue("assignment_uid", context.assignmentUid());
            var ceId = UuidUtils.toUuidOrNull(r.getCanonicalElementId());
            p.addValue("canonical_element_id", ceId);
            p.addValue("outbox_id", context.outboxId());
            p.addValue("ingest_id", context.ingestId());
            p.addValue("submission_id", context.submissionId());
            p.addValue("submission_uid", context.submissionUid());
            p.addValue("submission_serial_number", context.submissionSerial());
            p.addValue("submission_creation_time",
                context.submissionCreationTime() != null ? Timestamp.from(context.submissionCreationTime()) : null);
            p.addValue("start_time",
                context.startTime() != null ? Timestamp.from(context.startTime()) : null);
            p.addValue("template_uid", context.templateUid());
            p.addValue("template_version_uid", context.templateVersionUid());
            p.addValue("element_path", r.getElementPath());
            p.addValue("repeat_instance_id", r.getRepeatInstanceId());
            p.addValue("parent_instance_id", r.getParentInstanceId());
            p.addValue("repeat_index", r.getRepeatIndex());
            // sanitize value_text to 4000 chars
            String vt = r.getValueText();
            if (vt != null && vt.length() > VALUE_TEXT_MAX) vt = vt.substring(0, VALUE_TEXT_MAX) + "...(truncated)";
            p.addValue("value_text", vt);
            p.addValue("value_number", r.getValueNumber());
            p.addValue("value_bool", r.getValueBool());
            String vj = r.getValueJson();
            if (vj != null && vj.length() > VALUE_JSON_MAX) vj = vj.substring(0, VALUE_JSON_MAX) + "...(truncated)";
            p.addValue("value_json", vj);

            p.addValue("value_ref_type", r.getValueRefType());
            p.addValue("value_ref_uid", r.getValueRefUid());

            params[i++] = p;
        }

        // execute batch
        try {
            jdbc.batchUpdate(sql, params);
        } catch (Exception ex) {
            // debug: dump first parameter map to inspect what values caused the DB error
            try {
                Map<String, Object> first = params[0].getValues();
                log.error("Tall upsert failed. first-param keys: {}, sample values (trimmed): {}", first.keySet(),
                    first.entrySet().stream()
                        .map(e -> e.getKey() + "=" + (e.getValue() == null ? "null" : String.valueOf(e.getValue()).substring(0, Math.min(200, String.valueOf(e.getValue()).length()))))
                        .limit(10)
                        .collect(Collectors.joining(", ")));
            } catch (Exception e2) {
                log.error("Failed to dump first params: {}", e2.getMessage());
            }
            log.error("Tall upsert SQL error", ex);
            throw ex;
        }
    }
}

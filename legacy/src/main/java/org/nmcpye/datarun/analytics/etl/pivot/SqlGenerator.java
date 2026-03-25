package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("UnnecessaryLocalVariable")
@Component
@RequiredArgsConstructor
@Slf4j
public class SqlGenerator {

    private static final Locale LOCALE = Locale.ROOT;
    private final Naming naming;

    private String wrap(String s) {
        if (s == null) return null;
        return s.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase(LOCALE);
    }

    public String buildTemplateCreateSqlForBase(String baseFq, String templateUid, List<CanonicalElementWithConfig> ces) {
        String tableNew = Naming.newName(baseFq);

        String dims = String.join(", ",
            "e.deleted_at",
            "e.event_type",
            "e.event_name",
            "e.template_uid",

            "e.assigned_org_unit_gov",
            "e.assigned_org_unit_district",
            "e.assigned_org_unit_code",
            "e.assigned_org_unit_name",
            "e.activity_name",
            "e.assigned_team_code",
            "e.planned_day",
            "e.created_by_user",
            "e.assigned_users",
            "e.updated_at",
            "e.created_at",
            "e.submission_uid",
            "e.submission_serial",
            "e.parent_event_id",
            "e.event_ce_id",
            "e.event_id",
            "e.start_time",
            "e.submission_creation_time",
            "e.assigned_assignment_uid",
            "e.activity_uid",
            "e.assigned_team_uid",
            "e.assigned_org_unit_uid"
        );

        LinkedHashMap<String, String> exprs = new LinkedHashMap<>();
        for (CanonicalElementWithConfig c : ces) {
            String base = Optional.ofNullable(c.getSafeNameOverride()).filter(s -> !s.isBlank()).orElse(c.getCanonicalElementId());
            String alias = wrap(base);
            exprs.put(alias, getExpression(c, alias));
        }
        String pivotCols = String.join(",\n    ", exprs.values());

        if (exprs.isEmpty()) {
            String emptySql = String.format("DROP TABLE IF EXISTS %1$s; CREATE TABLE %1$s AS SELECT %2$s WHERE false;",
                tableNew, dims);
            return emptySql;
        }

        String sql = String.format("""
                DROP TABLE IF EXISTS %1$s;
                CREATE TABLE %1$s AS
                WITH events AS (
                  SELECT
                    e.deleted_at,
                    e.submission_serial,
                    e.event_type,
                    e.event_name,
                    e.assigned_org_unit_gov,
                    e.assigned_org_unit_district,
                    e.assigned_org_unit_name,
                    e.activity_name,
                    e.assigned_team_code,
                    e.planned_day,
                    e.assigned_org_unit_code,
                    e.submission_creation_time,
                    e.start_time,
                    e.template_uid,
                    e.submission_uid,
                    e.parent_event_id,
                    e.event_id,
                    e.assigned_assignment_uid,
                    e.activity_uid,
                    e.assigned_team_uid,
                    e.assigned_org_unit_uid,
                    e.created_by_user,
                    e.assigned_users,
                    e.event_ce_id,
                    e.updated_at,
                    e.created_at
                  FROM analytics.events_enriched e
                  WHERE e.template_uid = '%2$s'
                ),
                -- nearest lookup: find closest ancestor (distance ASC) that has a tall_canonical row
                nearest_vals AS (
                  SELECT
                    ea.target_event,
                    tc.canonical_element_id,
                    tc.value_text,
                    tc.value_number,
                    tc.value_bool,
                    tc.value_json,
                    tc.value_ref_uid,
                    row_number() OVER (
                      PARTITION BY ea.target_event, tc.canonical_element_id
                      ORDER BY ea.distance
                    ) AS rn
                  FROM analytics.event_ancestors ea
                  JOIN analytics.tall_canonical tc
                    ON tc.instance_key = ea.ancestor_event
                  WHERE ea.target_event IN (SELECT event_id FROM events)
                ),
                nearest AS (
                  SELECT target_event, canonical_element_id, value_text, value_number, value_bool, value_json, value_ref_uid
                  FROM nearest_vals WHERE rn = 1
                )
                SELECT
                  %3$s,
                  %4$s
                FROM events e
                LEFT JOIN nearest n
                    ON n.target_event = e.event_id
                GROUP BY
                  %3$s;
                """,
            tableNew,
            templateUid,
            dims,
            pivotCols
        );

        return sql;
    }

    private static String getExpression(CanonicalElementWithConfig c, String alias) {
        String ceId = Optional.ofNullable(c.getCanonicalElementId()).orElse("");
        String dt = Optional.ofNullable(c.getDataType()).orElse("").toLowerCase(Locale.ROOT);
        String st = Optional.ofNullable(c.getSemanticType()).orElse("").toLowerCase(Locale.ROOT);

        // expressions reference alias 'n' (nearest)
        String n_text = String.format("MAX(CASE WHEN n.canonical_element_id = '%s' THEN n.value_text END)", ceId);
        String n_num = String.format("MAX(CASE WHEN n.canonical_element_id = '%s' THEN n.value_number END)", ceId);
        String n_bool = String.format("MAX(CASE WHEN n.canonical_element_id = '%s' THEN n.value_bool::text END)", ceId);
        String n_json = String.format("MAX(CASE WHEN n.canonical_element_id = '%s' THEN n.value_json::text END)", ceId);
        String n_ref = String.format("MAX(CASE WHEN n.canonical_element_id = '%s' THEN n.value_ref_uid END)", ceId);

        // ref types -> label and uid
        if (!st.isEmpty() && (st.equals("option") || st.equals("team") || st.equals("orgunit")
            || st.equals("org_unit") || st.equals("activity") || st.equals("assignment"))) {

            String labelExpr = String.format("%s AS \"%s\"", n_text, alias);

            String uidCol = alias + "_uid";
            String uidExpr = String.format("%s AS \"%s\"", n_ref, uidCol);

            return labelExpr + ",\n    " + uidExpr;
        }

        // JSON / ARRAY (single-valued or multiselect stored as json) -- keep simple coalesce behavior
        if ("json".equals(dt) || "array".equals(dt)) {
            // single-valued: take nearest.value_json
            return String.format("(%s)::jsonb AS \"%s\"", n_json, alias);
        }

        // non-ref scalar types -> single expression with MAX(...) like before
        return switch (dt) {
            case "integer", "int", "numeric", "decimal", "float", "double" ->
                String.format("%s AS \"%s\"", n_num, alias);
            case "boolean", "bool" -> String.format("(%s)::bool AS \"%s\"", n_bool, alias);
            case "timestamp", "date", "timestamptz" -> String.format("%s AS \"%s\"", n_text, alias);
            default -> String.format("%s AS \"%s\"", n_text, alias);
        };
    }
}

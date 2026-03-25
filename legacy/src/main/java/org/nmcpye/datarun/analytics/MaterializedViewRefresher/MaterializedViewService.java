package org.nmcpye.datarun.analytics.MaterializedViewRefresher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaterializedViewService {
    private final Logger log = LoggerFactory.getLogger(MaterializedViewService.class);
    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    // Default mapping; you can inject/override this via constructor or config if needed.
    private final Map<String, String> viewToSourceMaxSql;

    public MaterializedViewService(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;

        // maintain insertion order (refresh order)
        Map<String, String> m = new LinkedHashMap<>();
        m.put("analytics.dim_option_set", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.option_set");
        m.put("analytics.dim_option", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.option_value");
        m.put("analytics.dim_org_unit", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.org_unit");
        m.put("analytics.dim_org_unit_group", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.org_unit_group");
        m.put("analytics.dim_org_unit_group_member", "SELECT NULL"); //fallback
        m.put("analytics.dim_team", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.team");
        m.put("analytics.dim_team_form_permission", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.team");
        m.put("analytics.dim_assignment", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.assignment");
        m.put("analytics.dim_activity", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.activity");
        m.put("analytics.dim_data_template", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.data_template");
        m.put("analytics.dim_malaria_unit_user_group", "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.user_group");

        m.put("analytics.ref_value_enriched", "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.ref_type_value");
        m.put("analytics.events_enriched", "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.events");
//        m.put("pivot.data_values_enriched", "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.tall_canonical");
        this.viewToSourceMaxSql = Collections.unmodifiableMap(m);
    }

    /**
     * Refresh one view. If 'onlyIfChanged' is true, checks analytics.mv_refresh_log and source timestamps.
     * Returns a Map with status info.
     */
    public Map<String, Object> refreshSingle(String viewName, boolean concurrent, boolean onlyIfChanged) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("view", viewName);
        try {
            Instant lastSource = queryInstantSafe(viewToSourceMaxSql.getOrDefault(viewName, "SELECT NULL"));
            Instant lastRefresh = queryLastRefresh(viewName);

            boolean doRefresh = true;
            if (onlyIfChanged && lastSource != null && lastRefresh != null) {
                doRefresh = lastSource.isAfter(lastRefresh);
            } else if (onlyIfChanged && lastSource == null && lastRefresh != null) {
                // unknown source-change -> skip unless never refreshed
                doRefresh = lastRefresh == null;
            }

            result.put("last_source_change", lastSource);
            result.put("last_refresh_before", lastRefresh);
            result.put("should_refresh", doRefresh);

            if (!doRefresh) {
                result.put("status", "skipped_no_change");
                return result;
            }

            boolean ok = tryRefresh(viewName, concurrent);
            if (!ok && concurrent) {
                // fallback to non-concurrent
                ok = tryRefresh(viewName, false);
            }

            if (ok) {
                Instant now = Instant.now();
                upsertRefreshLog(viewName, now, lastSource, null);
                result.put("status", "ok");
                result.put("refreshed_at", now);
            } else {
                upsertRefreshLog(viewName, null, lastSource, "refresh_failed");
                result.put("status", "failed");
            }
        } catch (Exception ex) {
            log.error("Error refreshing view {}: {}", viewName, ex.getMessage(), ex);
            upsertRefreshLog(viewName, null, null, ex.getMessage());
            result.put("status", "error");
            result.put("error", ex.getMessage());
        }
        return result;
    }

    /**
     * Refresh a list of views (in order) or all known views when views == null.
     */
    public Map<String, Map<String, Object>> refreshMany(List<String> views, boolean concurrent, boolean onlyIfChanged) {
        Map<String, Map<String, Object>> out = new LinkedHashMap<>();
        if (views == null || views.isEmpty()) {
            views = List.copyOf(viewToSourceMaxSql.keySet());
        }
        for (String view : views) {
            out.put(view, refreshSingle(view, concurrent, onlyIfChanged));
        }
        return out;
    }

    // --- helper methods below ---

    private Instant queryInstantSafe(String sql) {
        try {
            if (sql == null) return null;
            Timestamp ts = jdbc.queryForObject(sql, Timestamp.class);
            return ts == null ? null : ts.toInstant();
        } catch (Exception ex) {
            log.debug("Could not run source-max SQL [{}]: {} (returning null)", sql, ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private Instant queryLastRefresh(String viewName) {
        try {
            Timestamp t = jdbc.queryForObject("SELECT last_refresh FROM analytics.mv_refresh_log WHERE view_name = ?", new Object[]{viewName}, Timestamp.class);
            return t == null ? null : t.toInstant();
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean tryRefresh(String viewName, boolean concurrent) {
        String sql = concurrent ? String.format("REFRESH MATERIALIZED VIEW CONCURRENTLY %s", viewName)
            : String.format("REFRESH MATERIALIZED VIEW %s", viewName);
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            if (!conn.getAutoCommit()) conn.setAutoCommit(true);
            stmt.execute(sql);
            log.info("Refreshed {} (concurrent={})", viewName, concurrent);
            return true;
        } catch (Exception ex) {
            log.warn("Refresh failed for {} (concurrent={}): {}", viewName, concurrent, ex.getMessage());
            return false;
        }
    }

    private void upsertRefreshLog(String viewName, Instant lastRefresh, Instant lastSourceChange, String lastError) {
        try {
            jdbc.update(
                "INSERT INTO analytics.mv_refresh_log (view_name, last_refresh, last_source_change, last_error) VALUES (?, ?, ?, ?) " +
                    "ON CONFLICT (view_name) DO UPDATE SET last_refresh = EXCLUDED.last_refresh, last_source_change = EXCLUDED.last_source_change, last_error = EXCLUDED.last_error",
                viewName,
                lastRefresh == null ? null : Timestamp.from(lastRefresh),
                lastSourceChange == null ? null : Timestamp.from(lastSourceChange),
                lastError
            );
        } catch (Exception ex) {
            log.error("Failed to upsert refresh log for {}: {}", viewName, ex.getMessage(), ex);
        }
    }
}


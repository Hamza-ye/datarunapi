package org.nmcpye.datarun.analytics.MaterializedViewRefresher;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MaterializedViewRefresher {
    private final Logger log = LoggerFactory.getLogger(MaterializedViewRefresher.class);

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;

    /**
     * cron for the scheduled job. Default: every 15 minutes.
     * You can override in application.yml: analytics.refresh.cron
     */
    @Value("${analytics.refresh.cron:0 0/15 * * * *}")
    private String cron;

    /**
     * map of materialized view name -> SQL that returns the last source
     * modification timestamp (timestamptz).
     * You can extend/replace this map to match your actual sources.
     *
     * IMPORTANT: these SQL queries should return a single timestamptz/timestamp or
     * null.
     */
    private final Map<String, String> viewToSourceMaxSql = new LinkedHashMap<>();

    public MaterializedViewRefresher(JdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        // Default mappings — update these if your table names or timestamp columns
        // differ.
        viewToSourceMaxSql.put("analytics.dim_option_set",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.option_set");
        viewToSourceMaxSql.put("analytics.dim_option",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.option_value");
        viewToSourceMaxSql.put("analytics.dim_org_unit",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.org_unit");
        viewToSourceMaxSql.put("analytics.dim_org_unit_group",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.org_unit_group");
        viewToSourceMaxSql.put("analytics.dim_org_unit_group_member",
                "SELECT GREATEST(MAX(NULLIF((SELECT NULL),NULL)), NULL)"); // fallback — will always be null (no-op)
        viewToSourceMaxSql.put("analytics.dim_team",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.team");
        viewToSourceMaxSql.put("analytics.dim_team_form_permission",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.team");
        viewToSourceMaxSql.put("analytics.dim_assignment",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.assignment");
        viewToSourceMaxSql.put("analytics.dim_activity",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.activity");
        viewToSourceMaxSql.put("analytics.dim_data_template",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.data_template");

        viewToSourceMaxSql.put("analytics.dim_malaria_unit_user_group",
                "SELECT GREATEST(MAX(created_date), MAX(last_modified_date)) FROM public.user_group");

        viewToSourceMaxSql.put("analytics.ref_value_enriched",
                "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.ref_type_value");
        // for each other
        viewToSourceMaxSql.put("analytics.events_enriched",
                "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.events");
        viewToSourceMaxSql.put("analytics.event_ancestors",
                "SELECT GREATEST(MAX(created_at), MAX(updated_at)) FROM analytics.events");
    }

    @PostConstruct
    public void init() {
        log.info("MaterializedViewRefresher initialized. Cron: {}", cron);
        // You can modify viewToSourceMaxSql programmatically here (e.g., load from
        // properties)
    }

    /**
     * Scheduled trigger. Spaced by cron (default every 15 minutes).
     * Use application property analytics.refresh.cron to override.
     */
    @Scheduled(cron = "${analytics.refresh.cron:0 0/15 * * * *}")
    public void scheduledRefreshCheck() {
        log.debug("Running scheduled materialized view refresh check...");
        for (Map.Entry<String, String> e : viewToSourceMaxSql.entrySet()) {
            String view = e.getKey();
            String sourceMaxSql = e.getValue();
            try {
                refreshIfNeeded(view, sourceMaxSql);
            } catch (Exception ex) {
                log.error("Unhandled error while evaluating refresh for view {}: {}", view, ex.getMessage(), ex);
            }
        }
    }

    private void refreshIfNeeded(String viewName, String sourceMaxSql) {
        Instant lastSourceChange = queryInstantSafe(sourceMaxSql);
        Instant lastRefresh = queryLastRefresh(viewName);

        log.debug("View: {} | lastSourceChange={} lastRefresh={}", viewName, lastSourceChange, lastRefresh);

        boolean shouldRefresh;
        if (lastRefresh == null && lastSourceChange != null) {
            shouldRefresh = true;
        } else if (lastSourceChange == null && lastRefresh == null) {
            // no source change info and never refreshed — refresh once
            shouldRefresh = true;
        } else if (lastSourceChange == null) {
            // no source change info — we conservatively skip refresh
            log.info("No source-change timestamp available for {}, skipping refresh (no reliable source check).",
                    viewName);
            shouldRefresh = false;
        } else {
            shouldRefresh = lastRefresh == null || lastSourceChange.isAfter(lastRefresh);
        }

        if (shouldRefresh) {
            log.info("Refreshing materialized view {} — source changed at {}", viewName, lastSourceChange);
            boolean refreshed = tryRefresh(viewName, true); // try concurrent first
            if (!refreshed) {
                log.warn("Concurrent refresh failed for {}, trying non-concurrent refresh.", viewName);
                refreshed = tryRefresh(viewName, false);
            }

            if (refreshed) {
                upsertRefreshLog(viewName, Instant.now(), lastSourceChange, null);
            } else {
                upsertRefreshLog(viewName, null, lastSourceChange, "refresh_failed");
            }
        } else {
            log.debug("Skipping refresh for {} — no new changes.", viewName);
        }
    }

    /**
     * Query a SQL that returns a timestamp and convert to Instant, safely.
     */
    private Instant queryInstantSafe(String sql) {
        try {
            Timestamp ts = jdbc.queryForObject(sql, Timestamp.class);
            return ts == null ? null : ts.toInstant();
        } catch (Exception ex) {
            log.debug("Error while running source-max query [{}]: {} - returning null", sql, ex.getMessage());
            return null;
        }
    }

    /**
     * Read last_refresh from analytics.mv_refresh_log.
     */
    @SuppressWarnings("deprecation")
    private Instant queryLastRefresh(String viewName) {
        try {
            Timestamp t = jdbc.queryForObject(
                    "SELECT last_refresh FROM analytics.mv_refresh_log WHERE view_name = ?",
                    new Object[] { viewName },
                    Timestamp.class);
            return t == null ? null : t.toInstant();
        } catch (Exception ex) {
            // table may not have an entry yet or table not present — return null
            log.debug("Could not read last_refresh for {}: {}", viewName, ex.getMessage());
            return null;
        }
    }

    /**
     * Try to refresh the materialized view. If concurrent=true, use CONCURRENTLY.
     * Must be executed outside transaction. We get a raw connection and
     * setAutoCommit(true).
     *
     * Returns true if refresh succeeded.
     */
    private boolean tryRefresh(String viewName, boolean concurrent) {
        String sql = concurrent
                ? String.format("REFRESH MATERIALIZED VIEW CONCURRENTLY %s", viewName)
                : String.format("REFRESH MATERIALIZED VIEW %s", viewName);
        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            // Ensure not inside a transaction block
            if (!conn.getAutoCommit()) {
                conn.setAutoCommit(true);
            }
            stmt.execute(sql);
            log.info("Successfully refreshed {} (concurrent={})", viewName, concurrent);
            return true;
        } catch (Exception ex) {
            log.warn("Refresh failed for {} (concurrent={}): {}", viewName, concurrent, ex.getMessage());
            return false;
        }
    }

    /**
     * Upsert log: insert or update row for view_name in analytics.mv_refresh_log.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertRefreshLog(String viewName, Instant lastRefresh, Instant lastSourceChange, String lastError) {
        try {
            jdbc.update(
                    "INSERT INTO analytics.mv_refresh_log (view_name, last_refresh, last_source_change, last_error) " +
                            "VALUES (?, ?, ?, ?) " +
                            "ON CONFLICT (view_name) DO UPDATE SET last_refresh = EXCLUDED.last_refresh, last_source_change = EXCLUDED.last_source_change, last_error = EXCLUDED.last_error",
                    viewName,
                    lastRefresh == null ? null : Timestamp.from(lastRefresh),
                    lastSourceChange == null ? null : Timestamp.from(lastSourceChange),
                    lastError);
        } catch (Exception ex) {
            log.error("Failed to upsert refresh log for {}: {}", viewName, ex.getMessage(), ex);
        }
    }

    // Optionally expose a manual refresh API (not scheduled). Example helper:
    public void refreshAllNow() {
        log.info("Manual full refresh requested.");
        for (String view : Collections.unmodifiableSet(viewToSourceMaxSql.keySet())) {
            boolean refreshed = tryRefresh(view, true);
            if (!refreshed)
                tryRefresh(view, false);
            upsertRefreshLog(view, refreshed ? Instant.now() : null, null, refreshed ? null : "manual_refresh_failed");
        }
    }
}

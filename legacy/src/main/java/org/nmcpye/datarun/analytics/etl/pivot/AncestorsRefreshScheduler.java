package org.nmcpye.datarun.analytics.etl.pivot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.*;

@Component
public class AncestorsRefreshScheduler {

    private final JdbcTemplate jdbc;
    private final DataSource dataSource;
    private final ExecutorService executor;

    // tune this
    private static final int PARALLELISM = 4;

    public AncestorsRefreshScheduler(JdbcTemplate jdbc, DataSource dataSource,
                                     @Value("${ancestors.refresh.threads:4}") int threads) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.executor = Executors.newFixedThreadPool(Math.max(1, threads));
    }

    // run every 5 minutes - tune as needed (or use cron)
//    @Scheduled(fixedDelayString = "${ancestors.refresh.interval.ms:300000}")
    public void scheduleRefresh() {
        String sql = """
          SELECT DISTINCT e.template_uid
          FROM analytics.events_enriched e
          LEFT JOIN analytics.mv_refresh_log l ON l.view_name = e.template_uid
          WHERE l.last_refresh IS NULL OR e.updated_at > l.last_refresh
        """;

        List<String> templates = jdbc.queryForList(sql, String.class);
        if (templates.isEmpty()) return;

        List<? extends Future<?>> futures = templates.stream()
            .map(t -> executor.submit(() -> safeRefreshTemplate(t)))
            .toList();

        // optionally wait for completion with timeout
        for (Future<?> f : futures) {
            try { f.get(10, TimeUnit.MINUTES); }
            catch (TimeoutException te) { f.cancel(true); }
            catch (Exception ignore) { /* log as needed */ }
        }
    }

    private void safeRefreshTemplate(String templateUid) {
        // acquire advisory lock per-template and run the function on same connection
        // use hashtext to map string -> bigint deterministically
        String tryLockSql = "SELECT pg_try_advisory_lock(hashtext(?))";
        String releaseSql  = "SELECT pg_advisory_unlock(hashtext(?))";
        String callFuncSql = "SELECT analytics.refresh_template_ancestors(?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // advisory lock is session-scoped; keep session alive
            try (PreparedStatement psLock = conn.prepareStatement(tryLockSql)) {
                psLock.setString(1, templateUid);
                try (ResultSet rs = psLock.executeQuery()) {
                    if (rs.next() && rs.getBoolean(1)) {
                        // we got the lock -> call the refresh function using same connection
                        try (PreparedStatement psCall = conn.prepareStatement(callFuncSql)) {
                            psCall.setString(1, templateUid);
                            psCall.execute();
                        }
                        // update log (optional) - do within same session/transaction to make update atomic
                        try (PreparedStatement psUpdate = conn.prepareStatement(
                            "UPDATE analytics.mv_refresh_log SET last_refresh = now() WHERE view_name = ?; " +
                                "INSERT INTO analytics.mv_refresh_log(view_name, last_refresh) " +
                                "SELECT ?, now() " +
                                "WHERE NOT EXISTS (SELECT 1 FROM analytics.mv_refresh_log WHERE view_name = ?)"
                        )) {
                            psUpdate.setString(1, templateUid);
                            psUpdate.setString(2, templateUid);
                            psUpdate.setString(3, templateUid);
                            psUpdate.executeUpdate();
                        }
                        conn.commit();
                    } else {
                        // lock not acquired => another instance/process is handling it
                        conn.rollback();
                    }
                }
            } catch (Throwable ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                // log error
                throw new RuntimeException("refresh failed for " + templateUid, ex);
            } finally {
                // release lock if held
                try (PreparedStatement psRel = conn.prepareStatement(releaseSql)) {
                    psRel.setString(1, templateUid);
                    psRel.execute();
                } catch (Exception ignore) {}
            }
        } catch (Exception e) {
            // log failure: DB connectivity / etc.
            throw new RuntimeException(e);
        }
    }
}

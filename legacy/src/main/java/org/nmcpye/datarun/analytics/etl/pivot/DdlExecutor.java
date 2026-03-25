package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.RequiredArgsConstructor;

import org.nmcpye.datarun.sharedkernal.uidgenerate.CodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DdlExecutor {
    private static final Logger log = LoggerFactory.getLogger(DdlExecutor.class);

    private final JdbcTemplate jdbc;
    private final Naming naming;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(String sql, String baseFq) {
        Objects.requireNonNull(sql);
        log.info("Executing DDL (REQUIRES_NEW) - length={} chars", sql.length());
        // single-shot execute for generated DDL; keep simple
        jdbc.execute(sql);
        // committed by Spring when this method returns (REQUIRES_NEW)
        log.info("pivot: start creating indexes baseFq={}", baseFq);
        addIndexes(baseFq);
        log.info("pivot: indexes created for baseFq={}", baseFq);
        log.info("DDL executed and committed (REQUIRES_NEW)");
    }

    public void addIndexes(String tableName) {
        try {
            final String random = CodeGenerator.generateCode(5);
            jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS " + tableName.replace('.', '_') + "_" + random + "_event_id_idx ON " + tableName + " (event_id)");
            jdbc.execute("CREATE INDEX IF NOT EXISTS " + tableName.replace('.', '_') + "_" + random + "_updated_date_idx ON " + tableName + " (event_id, updated_at)");
        } catch (Exception e) {
            log.warn("Could not create indexes on {}: {}", tableName, e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atomicSwapTemplate(String templateUid) {
        String baseFq = naming.fqFactTableForTemplate(templateUid); // e.g. "analytics.fact_ggyx1cvnoaw"

        // split schema and table name
        String schema = "public";
        String baseName = baseFq;
        if (baseFq.contains(".")) {
            String[] parts = baseFq.split("\\.", 2);
            schema = parts[0];
            baseName = parts[1];
        }

        String newFq = Naming.newName(baseFq);   // analytics.fact_xxx_new
        String oldFq = Naming.oldName(baseFq);   // analytics.fact_xxx_old

        // table-only (required for RENAME TO target - must be unqualified)
        String newTableOnly = newFq.contains(".") ? newFq.substring(newFq.indexOf('.') + 1) : newFq;
        String oldTableOnly = oldFq.contains(".") ? oldFq.substring(oldFq.indexOf('.') + 1) : oldFq;

        // safety check: ensure new table exists before attempting a swap
        Boolean newExists = jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace WHERE n.nspname = ? AND c.relname = ?)",
            Boolean.class, schema, newTableOnly);
        if (newExists == null || !newExists) {
            throw new DataAccessResourceFailureException("Swap aborted: new table " + newFq + " does not exist");
        }

        String dropOld = String.format("DROP TABLE IF EXISTS %s;", oldFq); // schema-qualified ok
        // RENAME TO target must be table-only (no schema)
        String renameCurrentToOld = String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", baseFq, oldTableOnly);
        String renameNewToCurrent = String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", newFq, baseName);

        log.info("Performing atomic swap for template {} : {} -> {} (old {})", templateUid, newFq, baseFq, oldFq);

        jdbc.execute((ConnectionCallback<Void>) conn -> {
            boolean prevAutoCommit;
            try {
                prevAutoCommit = conn.getAutoCommit();
            } catch (SQLException ex) {
                throw new DataAccessResourceFailureException("Failed to get connection auto-commit", ex);
            }

            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(false);
                st.execute(dropOld);
                st.execute(renameCurrentToOld);
                st.execute(renameNewToCurrent);
                conn.commit();
                log.info("Atomic swap committed for template {}", templateUid);
                return null;
            } catch (SQLException ex) {
                log.error("pivot: build failed for template={}, reason={}", templateUid, ex.getMessage(), ex);
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    log.error("Rollback failed after swap error for template {}", templateUid, e);
                }
                throw new DataAccessResourceFailureException("atomic swap failed for template " + templateUid, ex);
            } finally {
                try {
                    conn.setAutoCommit(prevAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        });
    }

    /**
     * Atomic swap for activity (main + repeats). Same pattern as template swap.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atomicSwapActivity(String activityId) {
        String sanitized = Naming.sanitize(activityId);
        String baseFq = "pivot.fact_" + sanitized;
        String baseFqRepeats = Naming.repeatsTable(baseFq);

        String newFq = Naming.newName(baseFq);
        String oldFq = Naming.oldName(baseFq);

        String newFqRepeats = Naming.newName(baseFqRepeats);
        String oldFqRepeats = Naming.oldName(baseFqRepeats);

        log.info("Performing atomic swap for activity {} ({} + {} )", activityId, baseFq, baseFqRepeats);

        jdbc.execute((ConnectionCallback<Void>) conn -> {
            boolean prevAutoCommit;
            try {
                prevAutoCommit = conn.getAutoCommit();
            } catch (SQLException ex) {
                throw new DataAccessResourceFailureException("Failed to get connection auto-commit", ex);
            }

            try (Statement st = conn.createStatement()) {
                conn.setAutoCommit(false);

                st.execute(String.format("DROP TABLE IF EXISTS %s;", oldFq));
                st.execute(String.format("DROP TABLE IF EXISTS %s;", oldFqRepeats));

                st.execute(String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", baseFq, oldFq));
                st.execute(String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", baseFqRepeats, oldFqRepeats));

                st.execute(String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", newFq, baseFq));
                st.execute(String.format("ALTER TABLE IF EXISTS %s RENAME TO %s;", newFqRepeats, baseFqRepeats));

                conn.commit();
                log.info("Atomic swap committed for activity {}", activityId);
                return null;
            } catch (SQLException ex) {
                try {
                    conn.rollback();
                } catch (SQLException e) { /* log if you want */ }
                throw new DataAccessResourceFailureException("atomic swap failed for activity " + activityId, ex);
            } finally {
                try {
                    conn.setAutoCommit(prevAutoCommit);
                } catch (SQLException ignored) {
                }
            }
        });
    }
}

package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * Tracks pivot builds for templates/activities using standardized fact table names.
 */
@Repository
@RequiredArgsConstructor
public class PivotManifestRepository {

    private final JdbcTemplate jdbc;
    private final Naming naming;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startBuild(String templateUid, Instant startedAt) {
        String fqName = naming.fqFactTableForTemplate(templateUid);
        String sql = """
            INSERT INTO analytics.pivot_manifest(template_uid, view_name, version, status, build_started_at)
            VALUES (?, ?, 1, ?, ?)
            ON CONFLICT (template_uid) DO UPDATE
              SET status = EXCLUDED.status, build_started_at = EXCLUDED.build_started_at
            """;
        jdbc.update(sql, templateUid, fqName, "running", Timestamp.from(startedAt));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeBuild(String templateUid, java.time.Duration duration) {
        String sql = """
            UPDATE analytics.pivot_manifest
            SET status = ?,
                build_finished_at = now(),
                build_duration_secs = ?,
                notes = ?
            WHERE template_uid = ?
            """;
        jdbc.update(sql, "success", (int) duration.getSeconds(), "validation ok", templateUid);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failBuild(String templateUid, Object reason) {
        String sql = """
            UPDATE analytics.pivot_manifest
            SET status = ?,
                build_finished_at = now(),
                notes = ?
            WHERE template_uid = ?
            """;
        jdbc.update(sql, "failed", reason == null ? "failed" : reason.toString(), templateUid);
    }

    public StatusResponse getStatus(String templateUid) {
        String sql = """
            SELECT template_uid,
                   status,
                   build_started_at,
                   build_finished_at,
                   build_duration_secs,
                   columns_json,
                   notes
            FROM analytics.pivot_manifest
            WHERE template_uid = ?
            """;
        return jdbc.queryForObject(sql, new Object[]{templateUid}, (rs, rn) -> {
            StatusResponse s = new StatusResponse();
            s.setTemplateUid(rs.getString("template_uid"));
            s.setStatus(rs.getString("status"));
            s.setBuildStartedAt(rs.getTimestamp("build_started_at") == null
                ? null : rs.getTimestamp("build_started_at").toInstant());
            s.setBuildFinishedAt(rs.getTimestamp("build_finished_at") == null
                ? null : rs.getTimestamp("build_finished_at").toInstant());
            s.setDurationSecs(rs.getInt("build_duration_secs"));
            s.setNotes(rs.getString("notes"));
            return s;
        });
    }
}

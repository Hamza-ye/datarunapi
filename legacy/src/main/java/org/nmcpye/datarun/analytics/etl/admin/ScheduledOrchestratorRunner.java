package org.nmcpye.datarun.analytics.etl.admin;

import org.nmcpye.datarun.analytics.etl.orchestrator.EtlOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledOrchestratorRunner {

    private static final Logger log = LoggerFactory.getLogger(ScheduledOrchestratorRunner.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final EtlOrchestrator orchestrator;

    // choose a stable big int lock key (unique per cluster)
    private static final long ADVISORY_LOCK_KEY = 123456789012345L;

    @Value("${migration.size:100}")
    private int SCHEDULE_BATCH_SIZE;

    public ScheduledOrchestratorRunner(NamedParameterJdbcTemplate jdbc, EtlOrchestrator orchestrator) {
        this.jdbc = jdbc;
        this.orchestrator = orchestrator;
    }

    //

    @Scheduled(cron = "${migration.cron:0 */1 * * * *}")
    public void scheduledRun() {
        log.info("Checking for pending outbox events...");
        Integer countPending = jdbc.queryForObject(
            "SELECT COUNT(1) FROM outbox WHERE status = 'pending'",
            new MapSqlParameterSource(), Integer.class);
        if (countPending != null && countPending > 0) {
            log.info("{} pending outbox events, starting orchestrator run ...", countPending);
            Boolean acquired = jdbc.queryForObject(
                "SELECT pg_try_advisory_lock(:key)",
                new MapSqlParameterSource("key", ADVISORY_LOCK_KEY),
                Boolean.class
            );

            if (!Boolean.TRUE.equals(acquired)) {
                log.debug("Another instance holds the advisory lock; skipping scheduled run");
                return;
            }

            try {
                orchestrator.runOnce("scheduled", SCHEDULE_BATCH_SIZE);
            } finally {
                Boolean released = jdbc.queryForObject(
                    "SELECT pg_advisory_unlock(:key)",
                    new MapSqlParameterSource("key", ADVISORY_LOCK_KEY),
                    Boolean.class
                );
                log.info("Released advisory lock: {}", released);
            }
        } else {
            log.info("No pending outbox even");
        }
    }
}

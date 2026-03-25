package org.nmcpye.datarun.iam.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class LastSeenService {

    private final JdbcTemplate jdbc;

    // Acts as a buffer: UserID -> LastSeenTime
    // ConcurrentHashMap handles thread safety for us automatically
    private final Map<String, Instant> pendingUpdates = new ConcurrentHashMap<>();

    /**
     * Extremely fast operation. strictly in-memory.
     * No DB connection used here, so no latency for the user.
     */
    public void touch(String userId, Instant when) {
        if (userId == null) return;
        // Just put it in the map. If it exists, it updates the timestamp.
        pendingUpdates.put(userId, when);
    }

    /**
     * Runs every 2 minutes (120000ms).
     * Flushes all pending updates to DB in one efficient Batch.
     */
    @Scheduled(fixedRateString = "${application.last-seen.flush-interval:120000}")
    @Transactional
    public void flushBuffer() {
        if (pendingUpdates.isEmpty()) return;

        log.debug("Flushing last seen updates for {} users...", pendingUpdates.size());

        // 1. Snapshot the data and clear the buffer immediately so incoming requests can fill it again
        List<Object[]> batchArgs = new ArrayList<>();
        // Iterate safely
        for (String userId : pendingUpdates.keySet()) {
            Instant time = pendingUpdates.remove(userId); // Atomic remove
            if (time != null) {
                batchArgs.add(new Object[]{userId, Timestamp.from(time)});
            }
        }

        if (batchArgs.isEmpty()) return;

        try {
            // 2. Execute Batch Upsert
            String sql = """
                INSERT INTO public.user_last_seen (user_id, last_seen)
                VALUES (?, ?)
                ON CONFLICT (user_id)
                DO UPDATE SET last_seen = GREATEST(EXCLUDED.last_seen, public.user_last_seen.last_seen)
                """;

            int[] results = jdbc.batchUpdate(sql, batchArgs);
            log.info("Batch updated last_seen for {} users.", results.length);

        } catch (Exception e) {
            log.error("Failed to flush last_seen batch", e);
            // Optional: Re-add keys to pendingUpdates if strict accuracy is required,
            // but for 'last seen' it's usually okay to drop one cycle on error.
        }
    }
}

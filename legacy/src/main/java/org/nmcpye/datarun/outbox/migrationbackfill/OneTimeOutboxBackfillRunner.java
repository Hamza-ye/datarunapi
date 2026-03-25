package org.nmcpye.datarun.outbox.migrationbackfill;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.datacapture.datasubmission.DataSubmission;
import org.nmcpye.datarun.datacapture.datasubmission.repository.DataSubmissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OneTimeOutboxBackfillRunner implements CommandLineRunner {

    private final DataSubmissionRepository submissionRepository;
    private final NamedParameterJdbcTemplate jdbc;

    // Feature flag: default false for safety
    @Value("${migration.runOutboxBackfill:false}")
    private boolean enabled;

    // CSV list of submission UIDs to restrict to; empty string => process all
    @Value("${migration.submissionUids:}")
    private String submissionUidsCsv;

    @Value("${migration.size:500}")
    private int batchSize;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        if (!enabled) {
            log.info("Backfill disabled. Exiting.");
            return;
        }

        log.info("Starting OUTBOX backfill…");

        long lastSerial = queryRawCount();
//        long lastSerial = 0;
        Integer submissionsToBackfill = submissionRepository.countBySerialNumberGreaterThan(lastSerial);
        int scanned = 0;
        int insertedTotal = 0;

        log.info("Max OUTBOX serial: {}", lastSerial);
        log.info("Submissions to Back fill Count: {}", submissionsToBackfill);
        List<String> restrictUids = parseCsv(submissionUidsCsv);

        if (submissionsToBackfill > 0) {
            while (true) {
                Page<DataSubmission> page =
                    submissionRepository.findBySerialNumberGreaterThanAndFormInOrderBySerialNumberAsc(
                        lastSerial, restrictUids, PageRequest.of(0, batchSize)
                    );

                if (page.isEmpty()) break;
                List<DataSubmission> list = page.getContent();
                scanned += list.size();

                log.info("Scanned: {}", scanned);
                // collect serials
                List<Long> serials = list.stream()
                    .map(DataSubmission::getSerialNumber)
                    .filter(Objects::nonNull)
                    .toList();

                if (serials.isEmpty()) {
                    lastSerial = list.get(list.size() - 1).getSerialNumber();
                    continue;
                }

                // find which already exist
                Set<Long> existing = new HashSet<>(
                    jdbc.queryForList(
                        "SELECT submission_serial_number FROM outbox WHERE submission_serial_number IN (:serials)",
                        new MapSqlParameterSource("serials", serials),
                        Long.class
                    )
                );

                log.info("existing: {}", existing.size());
                List<MapSqlParameterSource> inserts = new ArrayList<>();

                for (DataSubmission s : list) {
                    Long serial = s.getSerialNumber();
                    if (serial == null || existing.contains(serial)) continue;

                    JsonNode dataPayload = s.getFormData();

//                    ObjectNode payload = objectMapper.createObjectNode();
//                    payload.put("submissionId", saved.getId());
//                    payload.put("submissionVersion", saved.getLockVersion());

                    MapSqlParameterSource p = new MapSqlParameterSource();
                    p.addValue("submission_serial_number", serial);
                    p.addValue("submission_id", s.getId());
                    p.addValue("submission_uid", s.getUid());
                    p.addValue("topic", s.getFormVersion());
                    p.addValue("event_type", "BACKFILL");
                    p.addValue("payload", dataPayload == null ? null : dataPayload.toString());
                    p.addValue("status", "pending");
                    p.addValue("attempt", 0);
                    p.addValue("created_at", Timestamp.from(Instant.now()));

                    inserts.add(p);
                }

                if (!inserts.isEmpty()) {

                    String sql = """
                            INSERT INTO outbox
                                (submission_serial_number, submission_id, submission_uid,
                                 topic, event_type, payload, status, attempt, created_at, claimed_at, claimed_by)
                            VALUES (:submission_serial_number, :submission_id, :submission_uid, :topic, :event_type,
                                    CAST(:payload AS jsonb), :status, :attempt, :created_at, NULL, NULL)
                            ON CONFLICT (submission_id) WHERE (event_type = 'BACKFILL') DO NOTHING
                        """;

                    jdbc.batchUpdate(sql, inserts.toArray(new MapSqlParameterSource[0]));
                    insertedTotal += inserts.size();

                    log.info("Inserted {} rows (lastSerial={})", inserts.size(),
                        list.get(list.size() - 1).getSerialNumber());
                }

                lastSerial = list.get(list.size() - 1).getSerialNumber();
            }
        }

        log.info("OUTBOX backfill done. scanned={}, inserted={}", scanned, insertedTotal);
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private long queryRawCount() {
        try {
            String sql = "SELECT MAX(submission_serial_number) FROM outbox;";
            var n = jdbc.queryForObject(sql, new MapSqlParameterSource(), Number.class);
            return n == null ? 0L : n.longValue();
        } catch (Exception ex) {
            log.warn("Error getting Max outboxed serial: {}", ex.getMessage());
            return -1L;
        }
    }
}

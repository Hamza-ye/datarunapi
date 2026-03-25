package org.nmcpye.datarun.web.rest.v1.etl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nmcpye.datarun.analytics.etl.admin.BackfillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Thin HTTP controller that delegates backfill enqueueing to BackfillService.
 * No JDBC here.
 */
@RestController
@RequestMapping("/admin/etl")
@RequiredArgsConstructor
@Slf4j
public class EnqueueBackfillController {

    private final BackfillService backfillService;

    public static class EnqueueRequest {
        public List<String> submissionIds;
        public Long fromSerial;
        public Long toSerial;
    }

    @PostMapping("/enqueue-backfill")
    public ResponseEntity<Map<String, Object>> enqueueBackfill(@RequestBody EnqueueRequest req) {
        if ((req.submissionIds == null || req.submissionIds.isEmpty())
            && (req.fromSerial == null || req.toSerial == null)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provide submissionIds or fromSerial/toSerial"));
        }

        int inserted;
        if (req.submissionIds != null && !req.submissionIds.isEmpty()) {
            inserted = backfillService.enqueueBySubmissionIds(req.submissionIds);
        } else {
            inserted = backfillService.enqueueBySerialRange(req.fromSerial, req.toSerial);
        }

        log.info("EnqueueBackfillController: inserted {} outbox backfill rows", inserted);
        return ResponseEntity.ok(Map.of("inserted", inserted));
    }
}


package org.nmcpye.datarun.web.rest.v1.etl;

import jakarta.annotation.PreDestroy;
import org.nmcpye.datarun.analytics.etl.orchestrator.EtlOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Small async controller to trigger EtlOrchestrator.runOnce(...) and query job status.
 * <p>
 * - Keeps an in-memory job status map. Replace with persistent job table if you need survivability across restarts.
 * - Uses a dedicated ExecutorService. You can inject a managed TaskExecutor instead if preferred.
 */
@RestController
@RequestMapping("/admin/etl")
public class AsyncEtlController {

    private static final Logger log = LoggerFactory.getLogger(AsyncEtlController.class);

    private final EtlOrchestrator orchestrator;
    private final ExecutorService executor;

    // jobId -> status message (RUNNING, SUCCESS, FAILED: shortMessage)
    private final ConcurrentMap<String, String> jobs = new ConcurrentHashMap<>();

    public AsyncEtlController(EtlOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        // lightweight thread pool for admin jobs; adjust core size if you expect multiple concurrent admin runs
        this.executor = Executors.newFixedThreadPool(2, runnable -> {
            Thread t = new Thread(runnable);
            t.setDaemon(false);
            t.setName("etl-admin-runner-" + t.getId());
            return t;
        });
    }

    public static class RunRequest {
        public String pipeline = "backfill";
        public int batchSize = 50;
        public boolean async = true; // kept for compatibility; endpoint always async
    }

    public static class RunResponse {
        public String jobId;
        public String status;
        public Instant startedAt;

        public RunResponse(String jobId, String status, Instant startedAt) {
            this.jobId = jobId;
            this.status = status;
            this.startedAt = startedAt;
        }
    }

    /**
     * Start an asynchronous run. Returns a jobId and initial status.
     * The run executes orchestrator.runOnce(pipeline, batchSize) in background.
     */
    @PostMapping("/run-async")
    public ResponseEntity<RunResponse> runAsync(@RequestBody(required = false) RunRequest req) {
        RunRequest r = req == null ? new RunRequest() : req;
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, "RUNNING");
        Instant startedAt = Instant.now();

        log.info("Enqueuing async ETL run jobId={} pipeline={} batchSize={}", jobId, r.pipeline, r.batchSize);

        executor.submit(() -> {
            try {
                orchestrator.runOnce(r.pipeline, r.batchSize);
                jobs.put(jobId, "SUCCESS");
                log.info("Async ETL run jobId={} completed SUCCESS", jobId);
            } catch (Throwable t) {
                String msg = t.getMessage() == null ? t.getClass().getSimpleName() : t.getMessage();
                // cap error length
                if (msg.length() > 400) msg = msg.substring(0, 400) + "...(truncated)";
                jobs.put(jobId, "FAILED: " + msg);
                log.error("Async ETL run jobId={} FAILED: {}", jobId, msg, t);
            }
        });

        return ResponseEntity.accepted().body(new RunResponse(jobId, "RUNNING", startedAt));
    }

    /**
     * Query job status by jobId.
     */
    @GetMapping("/run-status/{jobId}")
    public ResponseEntity<Map<String, String>> status(@PathVariable String jobId) {
        String status = jobs.get(jobId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("jobId", jobId, "status", status));
    }

    /**
     * Graceful shutdown of executor when bean destroyed.
     */
    @PreDestroy
    public void shutdown() {
        try {
            log.info("Shutting down AsyncEtlController executor...");
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}


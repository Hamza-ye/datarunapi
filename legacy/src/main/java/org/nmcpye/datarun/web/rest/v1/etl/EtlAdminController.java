package org.nmcpye.datarun.web.rest.v1.etl;

import org.nmcpye.datarun.analytics.etl.orchestrator.EtlOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/etl")
public class EtlAdminController {

    private final EtlOrchestrator orchestrator;

    public EtlAdminController(EtlOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    // Simple trigger: synchronous call returns after the run completes
    @PostMapping("/run")
    public ResponseEntity<String> runOnce(@RequestParam(defaultValue = "backfill") String pipeline,
            @RequestParam(defaultValue = "50") int batchSize) {
        orchestrator.runOnce(pipeline, batchSize);
        return ResponseEntity.ok("Run started and completed.");
    }

    // Kick off an async run if you want: (left as exercise)
    // For inspection: list runs via EtlRunService repository (implement a small
    // query)
}

package org.nmcpye.datarun.web.rest.v1.analytics;

import lombok.Getter;
import lombok.Setter;
import org.nmcpye.datarun.analytics.MaterializedViewRefresher.MaterializedViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics/refresh")
public class AnalyticsRefreshController {
    private final MaterializedViewService mvService;

    public AnalyticsRefreshController(MaterializedViewService mvService) {
        this.mvService = mvService;
    }

    /**
     * POST /api/analytics/refresh
     * Body (optional): {"views": ["analytics.dim_option_set","analytics.dim_activity"], "concurrent": true, "onlyIfChanged": true}
     * If views is omitted, refreshes all known views (in the service).
     */
    @PostMapping
    public ResponseEntity<Map<String, Map<String, Object>>> refresh(@RequestBody(required = false) RefreshRequest req) {
        List<String> views = req == null ? null : req.getViews();
        boolean concurrent = req == null || Boolean.TRUE.equals(req.getConcurrent());
        boolean onlyIfChanged = req == null || Boolean.TRUE.equals(req.getOnlyIfChanged());
        Map<String, Map<String, Object>> result = mvService.refreshMany(views, concurrent, onlyIfChanged);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/analytics/refresh/{viewNameEncoded}
     * Example: POST /api/analytics/refresh/analytics.dim_activity?concurrent=true&onlyIfChanged=false
     */
    @PostMapping("/{view}")
    public ResponseEntity<Map<String, Object>> refreshSingle(
        @PathVariable("view") String view,
        @RequestParam(value = "concurrent", defaultValue = "true") boolean concurrent,
        @RequestParam(value = "onlyIfChanged", defaultValue = "true") boolean onlyIfChanged) {
        Map<String, Object> res = mvService.refreshSingle(view, concurrent, onlyIfChanged);
        return ResponseEntity.ok(res);
    }

    @Getter
    @Setter
    public static class RefreshRequest {
        private List<String> views;
        private Boolean concurrent;
        private Boolean onlyIfChanged;
    }
}


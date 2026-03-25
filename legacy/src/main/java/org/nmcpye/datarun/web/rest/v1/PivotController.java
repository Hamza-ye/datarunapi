package org.nmcpye.datarun.web.rest.v1;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.analytics.etl.pivot.BuildResponse;
import org.nmcpye.datarun.analytics.etl.pivot.PivotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/v1/pivot")
@RequiredArgsConstructor
public class PivotController {
    private final PivotService pivotService;

//    @PostMapping("/build_by_template")
//    public ResponseEntity<BuildResponse> buildByTemplate(
//        @RequestParam(value = "template_uid") String templateUid) {
//        BuildResponse resp = pivotService.buildTemplate(templateUid);
//
//        return ResponseEntity.ok(resp);
//    }

    @PostMapping("/build_by_template")
    public ResponseEntity<Map<String, BuildResponse>> buildByTemplate(
        @RequestParam(value = "template_uid") List<String> templateUids) {

        // support comma-separated single param (spring gives single element list "a,b" if passed that way)
        // normalize and flatten any comma-separated values
        List<String> normalized = templateUids.stream()
            .filter(Objects::nonNull)
            .flatMap(s -> Arrays.stream(s.split(",")))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();

        if (normalized.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.emptyMap());
        }

        Map<String, BuildResponse> results = pivotService.buildTemplates(normalized);
        return ResponseEntity.ok(results);
    }
}

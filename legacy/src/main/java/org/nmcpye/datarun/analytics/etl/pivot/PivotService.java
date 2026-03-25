package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.RequiredArgsConstructor;
import org.nmcpye.datarun.template.datatemplate.repository.DataTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PivotService {
    private static final Logger log = LoggerFactory.getLogger(PivotService.class);

    private final CeMetadataRepository ceRepo;
    private final SqlGenerator sqlGen;
    private final DdlExecutor ddl;
    private final ValidationService validator;
    private final PivotManifestRepository manifestRepo;
    private final Naming naming;
    private final DataTemplateRepository templateRepository;

    public Map<String, BuildResponse> buildTemplates(List<String> uids) {
        if (uids == null || uids.isEmpty()) return Collections.emptyMap();

        Map<String, BuildResponse> results = new LinkedHashMap<>();
        for (String uid : uids) {
            try {
                // reuse existing logic which already reports to manifestRepo and logs
                BuildResponse resp = buildTemplate(uid);
                results.put(uid, resp);
            } catch (Throwable t) {
                // defensive: buildTemplate normally returns BuildResponse.failure on exceptions,
                // but catch anything that bubbles up to ensure we always return a result for every uid.
                log.error("pivot: unexpected failure while building template={}, reason={}", uid, t.getMessage(), t);
                results.put(uid, BuildResponse.failure(t));
            }
        }
        return results;
    }

    public BuildResponse buildTemplate(String uid) {
        Instant start = Instant.now();
        var template = templateRepository.findFirstByCodeOrUidOrId(uid, uid, uid)
            .orElseThrow(() -> new IllegalArgumentException("Unknown template: " + uid));

        var templateUid = template.getUid();

        manifestRepo.startBuild(templateUid, start);

        List<CanonicalElementWithConfig> ces = ceRepo.getElementsForTemplateWithConfig(templateUid)
            .stream()
            .filter(ce -> !"Repeat".equalsIgnoreCase(ce.getSemanticType()))
            .toList();

        if (ces.isEmpty()) {
            String msg = "No CE metadata found for template=" + templateUid;
            manifestRepo.failBuild(templateUid, msg);
            return BuildResponse.failure(msg);
        }

        // compute canonical base table name (pivot.fact_<sanitized>)
        String baseFq = naming.fqFactTableForTemplate(templateUid); // pivot.fact_<sanitized>
        // SqlGenerator is expected to create <baseFq>_new
        String createSql = sqlGen.buildTemplateCreateSqlForBase(baseFq, templateUid, ces);

        try {
            log.info("pivot: start build template={}, baseFq={}, sql-length={}", templateUid, baseFq, createSql.length());
            ddl.execute(createSql, Naming.newName(baseFq));
            log.info("pivot: DDL created (committed): template={}, checking validation", templateUid);

            log.debug("pivot: running validator.validateTemplateTable for baseFq={}, template={}", baseFq, templateUid);
            ValidationResult vr = validator.validateTemplateTable(baseFq, templateUid);
            if (!vr.passed()) {
                manifestRepo.failBuild(templateUid, vr);
                return BuildResponse.failure("Validation failed", vr, null);
            }

            log.info("pivot: validation passed, performing atomic swap for template={}", templateUid);
            ddl.atomicSwapTemplate(templateUid);
            log.info("pivot: atomic swap completed for template={}", templateUid);
            Duration duration = Duration.between(start, Instant.now());
            manifestRepo.completeBuild(templateUid, duration);
            return BuildResponse.success(duration.getSeconds());
        } catch (Exception ex) {
            manifestRepo.failBuild(templateUid, ex);
            log.error("pivot: build failed for template={}, reason={}", templateUid, ex.getMessage(), ex);
            return BuildResponse.failure(ex);
        }
    }

    public StatusResponse getStatus(String activity) {
        return manifestRepo.getStatus(activity);
    }
}

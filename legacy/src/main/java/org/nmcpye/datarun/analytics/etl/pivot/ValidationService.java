package org.nmcpye.datarun.analytics.etl.pivot;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ValidationService {
    private final JdbcTemplate jdbc;

    /**
     * Validate the just-created facts table (baseFq + "_new").
     *
     * @param baseFq      fully-qualified base name (e.g. "pivot.fact_sanitzed")
     * @param templateUid template uid (used for messages and additional checks)
     * @return ValidationResult.passed() or .failed(...)
     */
    public ValidationResult validateTemplateTable(String baseFq, String templateUid) {
        Objects.requireNonNull(baseFq, "baseFq required");

        // derive schema and table
        String schema = "pivot";
        String table = baseFq;
        if (baseFq.contains(".")) {
            String[] parts = baseFq.split("\\.", 2);
            schema = parts[0];
            table = parts[1];
        }

        String newTable = Naming.newName(schema + "." + table).replaceFirst("^" + schema + "\\.", "");
        // But to be safe, ensure newFqFormed correctly:
        String newFq = Naming.newName(schema + "." + table);
        // information_schema expects separate schema / table name pieces:
        String newTableNameOnly = newFq.contains(".") ? newFq.substring(newFq.indexOf('.') + 1) : newFq;

        // check existence
        Boolean exists = jdbc.queryForObject(
            "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)",
            Boolean.class, schema, newTableNameOnly);

        if (exists == null || !exists) {
            String msg = String.format("Validation failed: table %s does not exist (template=%s)", newFq, templateUid);
            log.warn(msg);
            return ValidationResult.failed(msg);
        }

        // required columns we expect in the pivot (adjust if you require more)
        List<String> required = Arrays.asList(
            "event_id", "template_uid", "submission_uid", "event_type"
        );

        // query present columns
        List<String> presentCols = jdbc.queryForList(
            "SELECT column_name FROM information_schema.columns WHERE table_schema = ? AND table_name = ?",
            String.class, schema, newTableNameOnly);

        Set<String> presentSet = presentCols.stream().map(String::toLowerCase).collect(Collectors.toSet());
        List<String> missing = required.stream()
            .filter(rc -> !presentSet.contains(rc.toLowerCase()))
            .collect(Collectors.toList());

        if (!missing.isEmpty()) {
            String msg = String.format("Validation failed: table %s missing required columns: %s", newFq, String.join(",", missing));
            log.warn(msg);
            return ValidationResult.failed(msg);
        }

        // optional: quick sanity checks (row count and example row)
        @SuppressWarnings("SqlSourceToSinkFlow") Integer rowCount = jdbc.queryForObject(
            String.format("SELECT count(*) FROM %s.%s", schema, newTableNameOnly),
            Integer.class);

        String note;
        if (rowCount == null) rowCount = 0;
        if (rowCount == 0) {
            note = String.format("Validation passed: %s exists and has required columns, but contains 0 rows. (template=%s)", newFq, templateUid);
            log.info(note);
            return ValidationResult.passed(note); // treat as pass but note it
        } else {
            note = String.format("Validation passed: %s exists, required columns present, rows=%d (template=%s)", newFq, rowCount, templateUid);
            log.info(note);
            return ValidationResult.passed(note);
        }
    }
}

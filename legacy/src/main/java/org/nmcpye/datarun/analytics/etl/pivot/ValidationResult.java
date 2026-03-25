package org.nmcpye.datarun.analytics.etl.pivot;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class ValidationResult {
    private final boolean passed;
    private final String message;

    private ValidationResult(boolean passed, String message) {
        this.passed = passed;
        this.message = message;
    }

    public boolean passed() { return passed; }
    public String message() { return message; }

    public static ValidationResult passed(String msg) { return new ValidationResult(true, msg); }
    public static ValidationResult failed(String msg) { return new ValidationResult(false, msg); }

    @Override
    public String toString() {
        return "ValidationResult{passed=" + passed + ", msg=" + message + "}";
    }
}

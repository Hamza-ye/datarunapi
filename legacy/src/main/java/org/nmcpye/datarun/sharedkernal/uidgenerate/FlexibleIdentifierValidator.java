package org.nmcpye.datarun.sharedkernal.uidgenerate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FlexibleIdentifierValidator implements ConstraintValidator<ValidFlexibleId, String> {
    // ULID: 26 chars, Crockford's Base32
    private static final String ULID_PATTERN = "^[0-9A-HJKMNP-TV-Z]{26}$";
    // UUID: 36 chars, standard hyphenated hex
    private static final String UUID_PATTERN = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank())
            return true; // Let @NotNull handle nulls
        String val = value.toUpperCase();
        return val.matches(ULID_PATTERN) || value.matches(UUID_PATTERN);
    }
}

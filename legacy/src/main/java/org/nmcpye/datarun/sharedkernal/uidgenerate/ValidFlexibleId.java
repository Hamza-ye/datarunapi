package org.nmcpye.datarun.sharedkernal.uidgenerate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FlexibleIdentifierValidator.class)
@Documented
public @interface ValidFlexibleId {
    String message() default "Invalid ID format. Must be a 26-char ULID or 36-char UUID.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

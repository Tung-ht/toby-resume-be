package com.tobyresume.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that each value in a Map&lt;String, String&gt; has length at most the given max.
 * Null or empty maps are valid.
 *
 * @see docs/ai/design/database-design.md §9
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MapValueMaxLengthValidator.class)
@Documented
public @interface MapValueMaxLength {

    int value();

    String message() default "Each value must be at most {value} characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

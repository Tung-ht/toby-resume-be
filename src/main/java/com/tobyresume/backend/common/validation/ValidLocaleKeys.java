package com.tobyresume.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a Map field has only keys "en" and "vi".
 * Use on Map&lt;String, String&gt; or Map&lt;String, List&lt;String&gt;&gt; etc.
 * Null or empty maps are valid.
 *
 * @see docs/ai/design/database-design.md §3
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocaleKeysValidator.class)
@Documented
public @interface ValidLocaleKeys {

    String message() default "Only locale keys 'en' and 'vi' are allowed";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

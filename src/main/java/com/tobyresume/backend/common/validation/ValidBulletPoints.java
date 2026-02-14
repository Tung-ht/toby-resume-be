package com.tobyresume.backend.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates Map&lt;String, List&lt;String&gt;&gt;: only locale keys en/vi, max list size per locale, max length per string.
 * Null or empty maps are valid.
 *
 * @see docs/ai/design/database-design.md §9 — Experience bulletPoints
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BulletPointsValidator.class)
@Documented
public @interface ValidBulletPoints {

    int maxItemsPerLocale() default 10;
    int maxLengthPerItem() default 500;

    String message() default "bulletPoints: max {maxItemsPerLocale} items per locale, each max {maxLengthPerItem} characters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

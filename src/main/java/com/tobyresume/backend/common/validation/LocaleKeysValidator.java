package com.tobyresume.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;
import java.util.Set;

/**
 * Ensures map keys are only "en" or "vi". Applied to localized content fields.
 *
 * @see docs/ai/design/database-design.md §3
 */
public class LocaleKeysValidator implements ConstraintValidator<ValidLocaleKeys, Map<?, ?>> {

    private static final Set<String> ALLOWED = Set.of("en", "vi");

    @Override
    public boolean isValid(Map<?, ?> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (Object key : value.keySet()) {
            if (!(key instanceof String) || !ALLOWED.contains((String) key)) {
                return false;
            }
        }
        return true;
    }
}

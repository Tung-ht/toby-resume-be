package com.tobyresume.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates bulletPoints map: keys en/vi only, list size per locale &lt;= maxItemsPerLocale, each string &lt;= maxLengthPerItem.
 */
public class BulletPointsValidator implements ConstraintValidator<ValidBulletPoints, Map<String, List<String>>> {

    private static final Set<String> ALLOWED_KEYS = Set.of("en", "vi");

    private int maxItemsPerLocale;
    private int maxLengthPerItem;

    @Override
    public void initialize(ValidBulletPoints annotation) {
        this.maxItemsPerLocale = annotation.maxItemsPerLocale();
        this.maxLengthPerItem = annotation.maxLengthPerItem();
    }

    @Override
    public boolean isValid(Map<String, List<String>> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (Map.Entry<String, List<String>> entry : value.entrySet()) {
            if (!ALLOWED_KEYS.contains(entry.getKey())) {
                return false;
            }
            List<String> list = entry.getValue();
            if (list != null) {
                if (list.size() > maxItemsPerLocale) {
                    return false;
                }
                for (String item : list) {
                    if (item != null && item.length() > maxLengthPerItem) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}

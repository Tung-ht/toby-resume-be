package com.tobyresume.backend.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class MapValueMaxLengthValidator implements ConstraintValidator<MapValueMaxLength, Map<String, String>> {

    private int maxLength;

    @Override
    public void initialize(MapValueMaxLength annotation) {
        this.maxLength = annotation.value();
    }

    @Override
    public boolean isValid(Map<String, String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true;
        }
        for (String v : value.values()) {
            if (v != null && v.length() > maxLength) {
                return false;
            }
        }
        return true;
    }
}

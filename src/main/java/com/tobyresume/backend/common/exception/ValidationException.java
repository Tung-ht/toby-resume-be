package com.tobyresume.backend.common.exception;

import com.tobyresume.backend.common.dto.ErrorBody;

import java.util.List;

/**
 * Thrown when input validation fails (business or DTO validation).
 * Mapped to 400 with error code VALIDATION_ERROR; details go in error.details.
 *
 * @see docs/ai/design/api-design.md §2.3
 */
public class ValidationException extends RuntimeException {

    private final String code;
    private final List<ErrorBody.FieldErrorDetail> details;

    public ValidationException(String message) {
        super(message);
        this.code = "VALIDATION_ERROR";
        this.details = null;
    }

    public ValidationException(String message, List<ErrorBody.FieldErrorDetail> details) {
        super(message);
        this.code = "VALIDATION_ERROR";
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public List<ErrorBody.FieldErrorDetail> getDetails() {
        return details;
    }
}

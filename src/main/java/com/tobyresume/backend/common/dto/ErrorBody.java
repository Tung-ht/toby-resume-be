package com.tobyresume.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Error branch of the REST envelope. code and message are required;
 * details is optional (e.g. validation field errors).
 *
 * @see docs/ai/design/api-design.md §2.2, §2.3
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorBody {

    private final String code;
    private final String message;
    private final List<FieldErrorDetail> details;

    public ErrorBody(String code, String message, List<FieldErrorDetail> details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public List<FieldErrorDetail> getDetails() {
        return details;
    }

    /**
     * Single field-level error for validation responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldErrorDetail {
        private final String field;
        private final String message;

        public FieldErrorDetail(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }
    }
}

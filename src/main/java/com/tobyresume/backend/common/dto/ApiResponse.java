package com.tobyresume.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Unified REST response envelope. All REST endpoints return this shape.
 * On success: success=true, data set, no error. On error: success=false, error set, no data.
 *
 * @see docs/ai/design/api-design.md §2
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    @JsonInclude(JsonInclude.Include.ALWAYS) // always include data on success (may be null)
    private final T data;
    private final ErrorBody error;
    private final String timestamp;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.timestamp = Instant.now().toString();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> error(ErrorBody error) {
        return new ApiResponse<>(false, null, error);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorBody getError() {
        return error;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

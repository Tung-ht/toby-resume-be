package com.tobyresume.backend.common.exception;

/**
 * Thrown when the user is authenticated but not allowed (e.g. not in allowed-admins).
 * Mapped to 403 and error code FORBIDDEN.
 *
 * @see docs/ai/design/api-design.md §2.3
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}

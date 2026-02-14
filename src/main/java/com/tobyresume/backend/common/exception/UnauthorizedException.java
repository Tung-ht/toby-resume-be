package com.tobyresume.backend.common.exception;

/**
 * Thrown when the request has no valid JWT or authentication is missing.
 * Mapped to 401 and error code UNAUTHORIZED.
 *
 * @see docs/ai/design/api-design.md §2.3
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.tobyresume.backend.common.exception;

/**
 * Thrown when a requested section or item does not exist.
 * Mapped to 404 and error code RESOURCE_NOT_FOUND.
 *
 * @see docs/ai/design/api-design.md §2.3
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

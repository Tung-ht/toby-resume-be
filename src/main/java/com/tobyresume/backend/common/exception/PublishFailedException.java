package com.tobyresume.backend.common.exception;

/**
 * Thrown when the publish pipeline fails. Mapped to 500 with code PUBLISH_FAILED.
 *
 * @see docs/ai/design/api-design.md §2.3
 */
public class PublishFailedException extends RuntimeException {

    public PublishFailedException(String message) {
        super(message);
    }

    public PublishFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.tobyresume.backend.common.exception;

import com.tobyresume.backend.common.dto.ApiResponse;
import com.tobyresume.backend.common.dto.ErrorBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps exceptions to the unified REST error envelope and appropriate HTTP status.
 *
 * @see docs/ai/design/api-design.md §2.2, §2.3
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(ForbiddenException ex) {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", null);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage(), ex.getDetails());
    }

    @ExceptionHandler(PublishFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePublishFailed(PublishFailedException ex) {
        log.warn("Publish pipeline failed: {}", ex.getMessage());
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "PUBLISH_FAILED", ex.getMessage(), null);
    }

    /**
     * Jakarta Bean Validation (@Valid) failures on request body.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ErrorBody.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorDetail)
                .collect(Collectors.toList());
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", null);
    }

    private ResponseEntity<ApiResponse<Void>> response(HttpStatus status, String code, String message,
                                                       List<ErrorBody.FieldErrorDetail> details) {
        ErrorBody error = new ErrorBody(code, message, details);
        return ResponseEntity.status(status).body(ApiResponse.error(error));
    }

    private ErrorBody.FieldErrorDetail toFieldErrorDetail(FieldError fe) {
        String field = fe.getObjectName();
        if (fe.getField() != null && !fe.getField().isEmpty()) {
            field = field + "." + fe.getField();
        }
        return new ErrorBody.FieldErrorDetail(field, fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid");
    }
}

package com.givebridge.exception;

import com.givebridge.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the GiveBridge API.
 * Catches exceptions thrown anywhere in the application and returns
 * clean, consistent JSON error responses instead of Spring's defaults.
 * Uses @RestControllerAdvice to intercept all controllers globally.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles EntityNotFoundException — thrown when a resource is not found.
     * Returns HTTP 404 Not Found.
     *
     * @param ex the exception
     * @return standardized error response with 404 status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles IllegalArgumentException — thrown when business rules are violated.
     * For example, donating to a campaign past its deadline.
     * Returns HTTP 400 Bad Request.
     *
     * @param ex the exception
     * @return standardized error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles MethodArgumentNotValidException — thrown when @Valid fails.
     * For example, sending a blank title or invalid email.
     * Returns HTTP 400 Bad Request with a map of field-level errors.
     *
     * @param ex the exception
     * @return map of field names to validation error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles any other unexpected exceptions not caught by specific handlers.
     * Acts as a safety net to prevent raw stack traces leaking to the client.
     * Returns HTTP 500 Internal Server Error.
     *
     * @param ex the exception
     * @return standardized error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected error occurred. Please try again later.")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
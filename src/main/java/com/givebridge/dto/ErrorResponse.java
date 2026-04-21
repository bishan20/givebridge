package com.givebridge.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO representing a standardized error response returned by the API.
 * Used by GlobalExceptionHandler to return consistent error formats
 * across all endpoints instead of Spring's default error response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * The HTTP status code of the error.
     */
    private int status;

    /**
     * A human-readable message describing what went wrong.
     */
    private String message;

    /**
     * Timestamp of when the error occurred.
     */
    private LocalDateTime timestamp;

    /**
     * Map of field-level validation errors.
     * Only populated when @Valid fails on DTO.
     * Key = field name, Value = validation error message.
     */
    private Map<String, String> fieldErrors;
}
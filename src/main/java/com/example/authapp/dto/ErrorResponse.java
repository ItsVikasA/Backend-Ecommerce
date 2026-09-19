package com.example.authapp.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Uniform error payload for every non-2xx response.
 *
 * <p>Shape:
 * <pre>{@code
 * {
 *   "timestamp":  "2026-09-16T14:30:00",
 *   "status":     400,
 *   "error":      "Bad Request",
 *   "message":    "Validation failed",
 *   "path":       "/auth/signup",
 *   "fieldErrors": {
 *     "username": "Username is required",
 *     "email":    "Email must be a valid email address"
 *   }
 * }
 * }</pre>
 *
 * <p>{@code fieldErrors} is only populated for 400s triggered by bean
 * validation. For all other errors it stays {@code null} and, thanks to
 * {@code spring.jackson.default-property-inclusion=non_null}, is stripped
 * from the JSON output entirely.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {

    /** Convenience factory for single-message errors (401, 404, 409, 500...). */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(LocalDateTime.now(), status, error, message, path, null);
    }

    /** Convenience factory for 400 validation failures with per-field messages. */
    public static ErrorResponse validation(String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), 400, "Bad Request", message, path, fieldErrors);
    }
}

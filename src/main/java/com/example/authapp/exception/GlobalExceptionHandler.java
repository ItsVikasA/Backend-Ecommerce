package com.example.authapp.exception;

import com.example.authapp.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Uniform HTTP error translation for the entire application.
 *
 * <p>Every non-2xx response goes through here and comes out shaped as
 * {@link ErrorResponse}. This is the sole place that decides:
 * <ul>
 *   <li>which exception maps to which HTTP status;</li>
 *   <li>what wording appears in the {@code message} field;</li>
 *   <li>at what log level a failure is recorded.</li>
 * </ul>
 *
 * <p>Two things this handler <b>does not</b> cover, by design:
 * <ul>
 *   <li><b>Missing / invalid JWT on protected endpoints.</b> That's caught
 *       by Spring Security's {@code ExceptionTranslationFilter} and routed
 *       to {@code JwtAuthEntryPoint} (STEP 10) before it ever reaches a
 *       controller or this advice.</li>
 *   <li><b>CORS preflight rejection.</b> Handled by Spring's CORS filter.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ============================================================
    //  400  Bad Request
    // ============================================================

    /**
     * {@code @Valid} on a {@code @RequestBody} DTO failed. Return per-field
     * messages so the frontend can render them next to the right inputs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                // First message wins if the same field has multiple violations.
                fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));

        log.debug("Validation failed at {}: {}", request.getRequestURI(), fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.validation(
                        "Validation failed", request.getRequestURI(), fieldErrors));
    }

    /** JSON was missing, malformed, or unreadable. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.debug("Unreadable request body at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed or missing request body", request);
    }

    /** A path variable or query param couldn't be converted (e.g. {@code /users/abc}). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value for parameter '" + ex.getName() + "'";
        log.debug("Type mismatch at {}: {} (raw value: {})",
                request.getRequestURI(), message, ex.getValue());
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Domain-level bad-request (currently only "password and confirm password
     * do not match" from {@code AuthService.signup}). The exception message
     * is intentional and safe to expose.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.debug("Illegal argument at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // ============================================================
    //  401  Unauthorized
    // ============================================================

    /**
     * Login failure. The wording is always the same generic message
     * regardless of whether the username was unknown or the password wrong.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        // AuthService already logged the specific reason server-side.
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // ============================================================
    //  403  Forbidden
    // ============================================================

    /**
     * Defensive. Currently no endpoint uses {@code @PreAuthorize} or checks
     * roles, so this rarely fires. Kept so future authorization additions
     * automatically get uniform 403 formatting.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "Access denied", request);
    }

    // ============================================================
    //  404  Not Found
    // ============================================================

    /** Domain-level "no such user". Message is safe (only reached after auth). */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        log.debug("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Spring MVC threw this because no {@code @RequestMapping} matches the URL. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoRoute(
            NoResourceFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "Endpoint not found", request);
    }

    // ============================================================
    //  405  Method Not Allowed
    // ============================================================

    /** The URL exists but not for this HTTP verb (e.g. GET /auth/signup). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        String message = "HTTP method " + ex.getMethod() + " is not allowed for this endpoint";
        return build(HttpStatus.METHOD_NOT_ALLOWED, message, request);
    }

    // ============================================================
    //  409  Conflict
    // ============================================================

    /** Duplicate username or duplicate email at signup. */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex, HttpServletRequest request) {
        log.debug("Duplicate registration at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Backstop: two signups race past the {@code existsBy*} checks and one
     * of them hits the DB's unique constraint. The generic 409 message avoids
     * leaking DB-specific SQLSTATE / constraint names.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation at {}: {}",
                request.getRequestURI(),
                ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "The requested change conflicts with existing data", request);
    }

    // ============================================================
    //  500  Internal Server Error
    // ============================================================

    /** Any Spring Data / Hibernate / JDBC failure other than data integrity. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabase(
            DataAccessException ex, HttpServletRequest request) {
        log.error("Database error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    /**
     * Catch-all. Never expose {@code ex.getMessage()} here — that's the one
     * place framework or JDK internals could leak into a response.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request);
    }

    // ============================================================
    //  Helper
    // ============================================================

    private static ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}

package com.example.authapp.exception;

/**
 * Thrown when a login attempt fails, regardless of whether the failure was
 * "user does not exist" or "password mismatch".
 *
 * <p>Mapped to HTTP 401 Unauthorized by {@code GlobalExceptionHandler} (STEP 12).
 *
 * <p>The message is deliberately generic — "Invalid username or password" —
 * so the API does not leak which specific fact was wrong. Together with the
 * BCrypt-timing mitigation in {@code AuthService.login()}, this closes the
 * common username-enumeration vector.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}

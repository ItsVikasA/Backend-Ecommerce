package com.example.authapp.exception;

/**
 * Thrown when signup would violate a unique constraint on {@code users}.
 *
 * <p>Mapped to HTTP 409 Conflict by {@code GlobalExceptionHandler} (STEP 12).
 *
 * <p>The static factories produce user-facing messages that are safe to
 * return to the client — they reveal that a username/email is taken, which
 * is inherent to signup (the client asked to create an account with that
 * identifier and needs to know the exact reason it failed).
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /** Duplicate-username variant. */
    public static UserAlreadyExistsException username(String username) {
        return new UserAlreadyExistsException(
                "Username '" + username + "' is already taken");
    }

    /** Duplicate-email variant. */
    public static UserAlreadyExistsException email(String email) {
        return new UserAlreadyExistsException(
                "Email '" + email + "' is already registered");
    }
}

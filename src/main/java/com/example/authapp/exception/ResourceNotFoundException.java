package com.example.authapp.exception;

/**
 * Thrown when a requested resource cannot be found by its identifier.
 *
 * <p>Mapped to HTTP 404 by {@code GlobalExceptionHandler} (STEP 12).
 *
 * <p>Deliberately used only for resources the caller is authorised to know
 * exists — e.g. their own profile via {@code /users/me} or an explicit id
 * lookup. It is <b>never</b> thrown from the login flow, where we always
 * return a generic 401 to avoid leaking username existence.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    /** Convenience factory for user-by-id lookups. */
    public static ResourceNotFoundException user(Long id) {
        return new ResourceNotFoundException("User with id " + id + " not found");
    }

    /** Convenience factory for user-by-username lookups. */
    public static ResourceNotFoundException user(String username) {
        return new ResourceNotFoundException("User with username '" + username + "' not found");
    }
}

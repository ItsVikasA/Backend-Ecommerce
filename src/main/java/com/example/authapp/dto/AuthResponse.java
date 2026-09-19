package com.example.authapp.dto;

/**
 * Response body for {@code POST /auth/login}.
 *
 * <p>Shape follows the OAuth 2.0 access-token response convention:
 * <ul>
 *   <li>{@code token}      — the signed JWT (without the "Bearer " prefix).</li>
 *   <li>{@code tokenType}  — always {@code "Bearer"}.</li>
 *   <li>{@code username}   — echoed back so the frontend can cache it for the Home page.</li>
 *   <li>{@code expiresIn}  — token lifetime in <b>seconds</b>, matching OAuth 2.0.</li>
 * </ul>
 *
 * <p>Deliberately does <i>not</i> include the user's id, email, phone, or
 * password. The frontend can hit {@code GET /users/me} for those.
 */
public record AuthResponse(
        String token,
        String tokenType,
        String username,
        long expiresIn
) {

    /** Factory for the standard Bearer token response. */
    public static AuthResponse bearer(String token, String username, long expiresInSeconds) {
        return new AuthResponse(token, "Bearer", username, expiresInSeconds);
    }
}

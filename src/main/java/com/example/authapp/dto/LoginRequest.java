package com.example.authapp.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /auth/login}.
 *
 * <p>Only presence is validated at this layer. Deeper checks (does the user
 * exist, does the password match) happen in {@code AuthService.login()} and
 * surface as a generic 401 to avoid leaking which specific fact was wrong
 * (username-not-found vs. wrong-password).
 */
public record LoginRequest(

        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "Password is required")
        String password
) {
}

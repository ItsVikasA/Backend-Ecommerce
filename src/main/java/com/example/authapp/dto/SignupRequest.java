package com.example.authapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /auth/signup}.
 *
 * <p>Every field is validated at the controller boundary via {@code @Valid}.
 * A failing validation short-circuits into {@code GlobalExceptionHandler}
 * (STEP 12) and returns a 400 with per-field messages.
 *
 * <p>The {@code confirmPassword} field is not compared to {@code password}
 * here — that equality check happens in {@code AuthService.signup()} because
 * cross-field validation is more naturally expressed as business logic
 * than as a bean-validation constraint.
 */
public record SignupRequest(

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9_]+$",
                message = "Username may contain only letters, digits, and underscores"
        )
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^\\+\\d{12}$",
                message = "Phone must be in format: +XX followed by 10 digits (e.g., +911234567890)"
        )
        String phone
) {
}

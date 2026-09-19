package com.example.authapp.controller;

import com.example.authapp.dto.AuthResponse;
import com.example.authapp.dto.LoginRequest;
import com.example.authapp.dto.SignupRequest;
import com.example.authapp.dto.UserResponse;
import com.example.authapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public authentication endpoints.
 *
 * <p>Both endpoints are declared {@code permitAll} in
 * {@code SecurityConfig#securityFilterChain}, so a JWT is <b>not</b>
 * required to reach them — otherwise nobody could ever sign up or log in.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /auth/signup} — create a new user.
 *       Returns 201 with a safe {@link UserResponse}.</li>
 *   <li>{@code POST /auth/login}  — verify credentials, issue a JWT.
 *       Returns 200 with an {@link AuthResponse}.</li>
 * </ul>
 *
 * <p>Every exception thrown from the service layer (validation, duplicate
 * username/email, bad credentials) is caught by {@code GlobalExceptionHandler}
 * (STEP 12) and translated to the uniform {@code ErrorResponse} shape.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     *
     * <p>Request body is validated via bean validation on {@link SignupRequest};
     * a validation failure short-circuits with 400 before {@code authService}
     * is ever invoked.
     *
     * @return 201 Created with the newly created user (no password)
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserResponse created = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Verify credentials and issue a signed JWT.
     *
     * @return 200 OK with { token, tokenType, username, expiresIn }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = authService.login(request);
        return ResponseEntity.ok(auth);
    }
}

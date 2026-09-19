package com.example.authapp.controller;

import com.example.authapp.dto.UserResponse;
import com.example.authapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User information endpoints.
 *
 * <p>All routes under {@code /users/**} require a valid JWT — enforced by
 * {@code SecurityConfig}'s {@code authorizeHttpRequests(... .anyRequest().authenticated())}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET /users/me}    — profile of the currently authenticated user.
 *       This is what {@code Home.jsx} calls on mount to prove the JWT still
 *       works and to display "Welcome, <username>".</li>
 *   <li>{@code GET /users/{id}}  — profile by id. Useful for admin flows
 *       and for direct API testing.</li>
 * </ul>
 *
 * <p>Path-mapping precedence: Spring resolves {@code /users/me} before
 * {@code /users/{id}} because exact matches take priority over path
 * variables — {@code me} is not accidentally parsed as an id.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Return the profile of the currently authenticated user.
     *
     * <p>The {@link UserDetails} is populated by {@code JwtAuthFilter} from
     * the JWT subject — no id is transmitted from the client.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponse me = userService.getByUsername(userDetails.getUsername());
        return ResponseEntity.ok(me);
    }

    /**
     * Return the profile of the user with the given id.
     *
     * @throws com.example.authapp.exception.ResourceNotFoundException  (→ 404)
     *         if no user has that id
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        UserResponse user = userService.getById(id);
        return ResponseEntity.ok(user);
    }
}

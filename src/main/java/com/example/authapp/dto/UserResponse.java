package com.example.authapp.dto;

import com.example.authapp.entity.User;

import java.time.LocalDateTime;

/**
 * Response body for {@code GET /users/{id}} and {@code GET /users/me}.
 *
 * <p>Safe projection of a {@link User} — deliberately omits the password
 * hash and the internal {@code updatedAt} audit column. This is the ONLY
 * user-shaped payload that leaves the backend.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String phone,
        LocalDateTime createdAt
) {

    /**
     * Map a persistent {@link User} entity to its safe API projection.
     * Kept on the DTO (rather than on User) so the entity stays free of
     * knowledge about the API layer.
     */
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getCreatedAt()
        );
    }
}

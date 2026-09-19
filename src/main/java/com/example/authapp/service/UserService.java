package com.example.authapp.service;

import com.example.authapp.dto.UserResponse;
import com.example.authapp.exception.ResourceNotFoundException;
import com.example.authapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only user retrieval service.
 *
 * <p>Sole responsibility: fetch a {@link com.example.authapp.entity.User}
 * from the database and expose it to callers as the safe
 * {@link UserResponse} projection.
 *
 * <p>The raw {@code User} entity never leaves this class — the password
 * hash and any other internal fields cannot leak through this API.
 *
 * <p>Authentication concerns (signup, login, password hashing, JWT issuance)
 * live in {@code AuthService} (STEP 9). Spring Security's user-loading
 * concern lives in {@code CustomUserDetailsService} (STEP 10). Both of those
 * bypass this service and talk to {@link UserRepository} directly, because
 * they need the entity itself, not the DTO.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Retrieve a user by primary key.
     *
     * @param id the user id (must not be {@code null})
     * @return the safe user projection
     * @throws ResourceNotFoundException if no user has that id
     */
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> ResourceNotFoundException.user(id));
    }

    /**
     * Retrieve a user by their unique username.
     *
     * <p>Used by {@code GET /users/me} after the JWT filter has already
     * authenticated the caller — so throwing 404 here does not leak
     * username-existence information to unauthenticated attackers.
     *
     * @param username the username (must not be {@code null} or blank)
     * @return the safe user projection
     * @throws ResourceNotFoundException if no user has that username
     */
    public UserResponse getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserResponse::fromEntity)
                .orElseThrow(() -> ResourceNotFoundException.user(username));
    }
}

package com.example.authapp.repository;

import com.example.authapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 *
 * <p>Spring Data parses these method names at startup and generates a
 * runtime proxy that issues the corresponding JPQL / SQL. No implementation
 * class exists on disk.
 *
 * <p>Query mapping (one method per real use case):
 * <ul>
 *   <li>{@link #findByUsername(String)}   —
 *       used by {@code AuthService.login()} and
 *       {@code CustomUserDetailsService.loadUserByUsername()}
 *       (JWT filter on every protected request).</li>
 *   <li>{@link #existsByUsername(String)} —
 *       used by {@code AuthService.signup()} to reject duplicate usernames
 *       before attempting an INSERT.</li>
 *   <li>{@link #existsByEmail(String)}    —
 *       used by {@code AuthService.signup()} to reject duplicate emails
 *       before attempting an INSERT.</li>
 * </ul>
 *
 * <p>{@code findById}, {@code save}, {@code deleteById}, {@code count},
 * and the rest of the CRUD surface are inherited from {@link JpaRepository}.
 * We do not need to redeclare them.
 *
 * <p>No {@code @Repository} annotation is used: Spring Data auto-detects
 * this interface via {@code @EnableJpaRepositories} (implicit in Spring
 * Boot) and already applies persistence-exception translation.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Look up a user by their unique username. */
    Optional<User> findByUsername(String username);

    /** Duplicate-username check for the signup flow. */
    boolean existsByUsername(String username);

    /** Duplicate-email check for the signup flow. */
    boolean existsByEmail(String email);
}

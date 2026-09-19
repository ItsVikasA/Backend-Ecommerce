package com.example.authapp.security;

import com.example.authapp.entity.User;
import com.example.authapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bridges our persistent {@link User} entity to Spring Security's
 * {@link UserDetails} abstraction.
 *
 * <p>Invoked by {@code JwtAuthFilter} on every authenticated request to
 * look up the user identified by the JWT subject. Also implicitly used by
 * Spring Boot's default {@code AuthenticationManager}, though our
 * {@code AuthService.login()} does not go through that path.
 *
 * <p>Deliberately talks to {@link UserRepository} directly rather than
 * through {@code UserService}: we need the raw password hash and the full
 * entity, not the safe DTO projection.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES =
            List.of(new SimpleGrantedAuthority("ROLE_USER"));

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load a user by username. Called by Spring Security machinery.
     *
     * @throws UsernameNotFoundException if no user has that username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())     // BCrypt hash from the DB
                .authorities(DEFAULT_AUTHORITIES)
                .accountLocked(false)
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}

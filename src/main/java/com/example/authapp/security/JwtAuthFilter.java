package com.example.authapp.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads {@code Authorization: Bearer <jwt>} on every request, validates the
 * token, and populates the {@link SecurityContextHolder} with an
 * authenticated principal.
 *
 * <p>Runs before Spring Security's {@code UsernamePasswordAuthenticationFilter}
 * (see {@code SecurityConfig#securityFilterChain}). If no token is present
 * or the token is rejected, the filter simply forwards the request without
 * authenticating it — the entry point handles the 401 downstream when the
 * request finally hits a protected endpoint.
 *
 * <p>Extends {@link OncePerRequestFilter} so it runs exactly once per HTTP
 * request even in the presence of forwards/dispatches.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService,
                         CustomUserDetailsService userDetailsService) {
        this.jwtService         = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // No token, or not a Bearer token — nothing to do at this layer.
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token);  // throws JwtException on failure

            // Only authenticate if the context is currently empty. Defensive:
            // if some upstream filter has already authenticated, don't overwrite.
            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,                       // no credentials retained after auth
                                    userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException e) {
            // Malformed / expired / bad signature — drop the token, let the
            // entry point return 401 if the endpoint is protected.
            log.debug("JWT rejected: {}", e.getMessage());
        } catch (UsernameNotFoundException e) {
            // Token's subject is well-formed but no such user exists (deleted?).
            log.debug("JWT subject not found in DB: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

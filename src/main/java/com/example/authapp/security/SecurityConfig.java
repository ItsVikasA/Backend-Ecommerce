package com.example.authapp.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Complete Spring Security 7 configuration.
 *
 * <p>Beans defined:
 * <ul>
 *   <li>{@link #passwordEncoder}         — BCrypt, strength 10.</li>
 *   <li>{@link #securityFilterChain}     — the request pipeline.</li>
 *   <li>{@link #corsConfigurationSource} — allow-list for the frontend origin.</li>
 *   <li>{@link #jwtAuthFilterRegistration} — disables the servlet-container
 *       auto-registration of {@link JwtAuthFilter}; only the security chain
 *       invokes it.</li>
 * </ul>
 *
 * <p>Filter-chain shape:
 * <pre>
 *   CORS  ─►  no-CSRF  ─►  no-session  ─►  authorize (auth/** public, else authenticated)
 *         ─►  JwtAuthFilter (BEFORE UsernamePasswordAuthenticationFilter)
 *         ─►  exception handling: 401 via JwtAuthEntryPoint
 * </pre>
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final String allowedOriginsProperty;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          JwtAuthEntryPoint jwtAuthEntryPoint,
                          @Value("${app.cors.allowed-origins}") String allowedOriginsProperty) {
        this.jwtAuthFilter          = jwtAuthFilter;
        this.jwtAuthEntryPoint      = jwtAuthEntryPoint;
        this.allowedOriginsProperty = allowedOriginsProperty;
    }

    // -----------------------------------------------------------------------
    //  Password hashing
    // -----------------------------------------------------------------------

    /**
     * BCrypt with strength 10 — Spring Security's default. Approximately
     * 100-200 ms per hash on modern hardware: slow enough to punish
     * brute-force, fast enough for interactive login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // -----------------------------------------------------------------------
    //  The filter chain
    // -----------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CORS; picks up the CorsConfigurationSource bean below.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless JWT auth — no cookies to protect from CSRF.
                .csrf(AbstractHttpConfigurer::disable)

                // No HttpSession at all; every request re-authenticates via the JWT.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 401 for anything that reaches an authenticated endpoint without auth.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))

                // Authorization rules.
                .authorizeHttpRequests(auth -> auth
                        // Public: signup + login.
                        .requestMatchers(HttpMethod.POST, "/auth/signup", "/auth/login").permitAll()
                        // Public: CORS preflight — the browser sends OPTIONS with no auth.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Everything else requires a valid JWT.
                        .anyRequest().authenticated()
                )

                // Insert our JWT filter before Spring's username/password filter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // -----------------------------------------------------------------------
    //  CORS
    // -----------------------------------------------------------------------

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origins = Arrays.stream(allowedOriginsProperty.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);   // cache preflight for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // -----------------------------------------------------------------------
    //  Disable Servlet-container auto-registration of JwtAuthFilter
    // -----------------------------------------------------------------------

    /**
     * {@link JwtAuthFilter} is a Spring bean (annotated {@code @Component})
     * so it can be constructor-injected here. Spring Boot's servlet
     * auto-configuration would otherwise also register it into the general
     * servlet chain, causing it to run twice per request. This registration
     * bean tells Spring Boot: don't register this filter yourself — the
     * security chain already does.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtAuthFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}

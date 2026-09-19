package com.example.authapp.service;

import com.example.authapp.dto.AuthResponse;
import com.example.authapp.dto.LoginRequest;
import com.example.authapp.dto.SignupRequest;
import com.example.authapp.dto.UserResponse;
import com.example.authapp.entity.User;
import com.example.authapp.exception.InvalidCredentialsException;
import com.example.authapp.exception.UserAlreadyExistsException;
import com.example.authapp.repository.UserRepository;
import com.example.authapp.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service — signup, login, password verification, JWT issuance.
 *
 * <p>Kept separate from {@link UserService} per the spec:
 * <ul>
 *   <li>{@code UserService} answers "who is user X?"  (read-only DTO surface)</li>
 *   <li>{@code AuthService} answers "create user X" or "prove you are user X"</li>
 * </ul>
 *
 * <p>Talks to {@link UserRepository} directly (not through {@code UserService})
 * because signup needs to persist an entity and login needs the raw password
 * hash — neither of which fits UserService's DTO-only API.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Pre-computed valid BCrypt hash used only for constant-time comparison
     * in the "user not found" branch of {@link #login}. Its plaintext does
     * not matter — the point is to make {@code passwordEncoder.matches()}
     * spend the same time whether or not the user exists, preventing
     * username enumeration via response-time analysis.
     */
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    // =======================================================================
    //  Signup
    // =======================================================================

    /**
     * Register a new user.
     *
     * <ol>
     *   <li>Confirm password matches password (belt-and-suspenders on top of
     *       the frontend check).</li>
     *   <li>Reject duplicate username → 409.</li>
     *   <li>Reject duplicate email → 409.</li>
     *   <li>BCrypt-hash the password.</li>
     *   <li>{@code INSERT INTO users} via the repository.</li>
     *   <li>Map to {@link UserResponse} (password never leaves the service).</li>
     * </ol>
     *
     * @throws IllegalArgumentException     if password ≠ confirmPassword
     * @throws UserAlreadyExistsException   if username or email is taken
     */
    @Transactional
    public UserResponse signup(SignupRequest request) {

        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException(
                    "Password and confirm password do not match");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw UserAlreadyExistsException.username(request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw UserAlreadyExistsException.email(request.email());
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(
                request.username(),
                passwordHash,
                request.email(),
                request.phone()
        );

        User saved = userRepository.save(user);
        log.info("New user registered: id={}, username={}",
                saved.getId(), saved.getUsername());
        return UserResponse.fromEntity(saved);
    }

    // =======================================================================
    //  Login
    // =======================================================================

    /**
     * Verify credentials and issue a JWT.
     *
     * <p>Both failure modes — unknown user and wrong password — throw the
     * same {@link InvalidCredentialsException} with the same generic message.
     * The API must not tell an attacker <i>which</i> fact was wrong.
     *
     * <p>Timing note: the "user not found" branch still executes one BCrypt
     * comparison (against a dummy hash) so the endpoint responds in
     * approximately the same time in both branches, mitigating username
     * enumeration via response-time analysis.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.username()).orElse(null);

        if (user == null) {
            // Constant-time mitigation: run a BCrypt compare against a valid
            // dummy hash so the "unknown user" response time matches the
            // "wrong password" response time.
            passwordEncoder.matches(request.password(), DUMMY_BCRYPT_HASH);
            log.warn("Login failed (unknown user): username={}", request.username());
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed (wrong password): username={}", request.username());
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getUsername());
        long expiresInSeconds = jwtService.getExpirationSeconds();

        log.info("User logged in: username={}", user.getUsername());
        return AuthResponse.bearer(token, user.getUsername(), expiresInSeconds);
    }
}

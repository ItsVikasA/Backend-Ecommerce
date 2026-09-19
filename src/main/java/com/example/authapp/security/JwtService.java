package com.example.authapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

/**
 * Issues and verifies JSON Web Tokens using HS256 (HMAC-SHA256).
 *
 * <p>The signing key is derived from {@code app.jwt.secret}, which must be
 * a Base64-encoded string that decodes to at least 32 bytes (256 bits) —
 * enforced at startup in {@link #initSigningKey()}. If the secret is
 * missing, malformed, or too short, the application refuses to start.
 *
 * <p>Every issued token contains only three claims:
 * <ul>
 *   <li>{@code sub} — the username</li>
 *   <li>{@code iat} — issued-at timestamp</li>
 *   <li>{@code exp} — expiration timestamp</li>
 * </ul>
 * We deliberately omit email, phone, and user id from the payload — those
 * are looked up from the database when needed. Result: smaller tokens and
 * no PII in the JWT.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final String secret;
    private final long expirationMs;

    /** Built once in {@link #initSigningKey()} after property injection. */
    private SecretKey signingKey;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.secret       = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * Decode the configured Base64 secret and build the HMAC signing key.
     * Fails loudly if the secret is missing, malformed, or too short.
     */
    @PostConstruct
    void initSigningKey() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret must not be blank. " +
                    "Set the JWT_SECRET environment variable.");
        }

        final byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "app.jwt.secret must be a valid Base64-encoded string", e);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret must decode to at least 32 bytes (256 bits) for HS256. " +
                    "Generate one with: openssl rand -base64 48");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT signing key initialised: {} bits, HS256, {}s expiration",
                keyBytes.length * 8, getExpirationSeconds());
    }

    // -----------------------------------------------------------------------
    // Issuance
    // -----------------------------------------------------------------------

    /**
     * Issue a signed JWT for the given username.
     *
     * @param username value placed in the {@code sub} claim
     * @return the compact, signed JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /** Configured expiration in seconds (used by {@code AuthResponse.expiresIn}). */
    public long getExpirationSeconds() {
        return Duration.ofMillis(expirationMs).toSeconds();
    }

    // -----------------------------------------------------------------------
    // Verification (used by JwtAuthFilter in STEP 10)
    // -----------------------------------------------------------------------

    /**
     * Verify the token's signature and expiration, then return its subject.
     *
     * @throws JwtException if the token is malformed, expired, or has an
     *                      invalid signature
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * True iff the token parses, is not expired, and its {@code sub} claim
     * matches the given username. Never throws — the filter prefers a
     * boolean check.
     */
    public boolean isTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = parseClaims(token);
            return expectedUsername != null
                    && expectedUsername.equals(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.example.authapp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA entity mapped to the {@code users} table.
 *
 * <p>Mirrors {@code database/schema.sql} exactly. Hibernate runs with
 * {@code spring.jpa.hibernate.ddl-auto=validate}, so any drift between this
 * class and the live table (missing column, wrong length, missing unique
 * constraint) causes application startup to fail with a clear error.
 *
 * <p>The {@code password} field stores a <b>BCrypt hash</b>, never a plain
 * password. It is annotated {@link JsonIgnore} so Jackson refuses to
 * serialize it, even if a controller accidentally returned this entity
 * directly. DTO mapping in the service layer is still the primary safeguard;
 * this annotation is a backstop.
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email",    columnNames = "email")
        }
)
public class User {

    /** Surrogate primary key; auto-assigned by MySQL AUTO_INCREMENT. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    /** Login handle. Unique across the users table. */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt hash of the user's password. Never plain text. Never serialized. */
    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /** Unique email address. */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** Contact phone number. Stored as VARCHAR to preserve leading zeros / '+'. */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** Row-insertion timestamp; populated by Hibernate on persist. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Row-modification timestamp; auto-updated by Hibernate on every merge. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // -----------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------

    /** Required by JPA. Do not use directly in application code. */
    public User() {
    }

    /**
     * Convenience constructor for new-user creation flow (AuthService.signup).
     * The {@code password} argument must already be the BCrypt hash.
     */
    public User(String username, String password, String email, String phone) {
        this.username = username;
        this.password = password;
        this.email    = email;
        this.phone    = phone;
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public Long getId()                 { return id; }
    public String getUsername()         { return username; }
    public String getPassword()         { return password; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // -----------------------------------------------------------------------
    // Setters
    // -----------------------------------------------------------------------

    public void setId(Long id)                        { this.id = id; }
    public void setUsername(String username)          { this.username = username; }
    public void setPassword(String password)          { this.password = password; }
    public void setEmail(String email)                { this.email = email; }
    public void setPhone(String phone)                { this.phone = phone; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

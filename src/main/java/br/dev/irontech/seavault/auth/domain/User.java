package br.dev.irontech.seavault.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String email;

    @Column(name = "password_hash", nullable = false)
    public String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public UserRole role;

    @Column(name = "email_verified", nullable = false)
    public boolean emailVerified;

    @Column(name = "terms_accepted_at")
    public Instant termsAcceptedAt;

    @Column(name = "terms_version")
    public String termsVersion;

    @Column(nullable = false)
    public String locale;

    @Column(nullable = false)
    public String country;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (locale == null) locale = "pt-BR";
        if (country == null) country = "BR";
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

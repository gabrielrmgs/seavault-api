package br.dev.irontech.seavault.profile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column
    public String cir;

    @Column
    @Convert(converter = br.dev.irontech.seavault.common.security.CryptoConverter.class)
    public String cpf;

    @Column
    @Convert(converter = br.dev.irontech.seavault.common.security.CryptoConverter.class)
    public String rg;

    @Column
    public String nationality;

    @Column
    @Convert(converter = br.dev.irontech.seavault.common.security.CryptoConverter.class)
    public String phone;

    @Column(name = "emergency_contact")
    @Convert(converter = br.dev.irontech.seavault.common.security.CryptoConverter.class)
    public String emergencyContact;

    @Column(name = "category_id")
    public UUID categoryId;

    @Column(name = "target_category_id")
    public UUID targetCategoryId;

    @Column(name = "completion_percent", nullable = false)
    public int completionPercent;

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
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

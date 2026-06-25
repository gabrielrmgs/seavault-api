package br.dev.irontech.seavault.files.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_links")
public class FileLink {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "file_id", nullable = false)
    public UUID fileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    public OwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    public UUID ownerId;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}

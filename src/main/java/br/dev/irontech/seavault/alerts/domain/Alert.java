package br.dev.irontech.seavault.alerts.domain;

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
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertSource source;

    @Column(name = "source_id", nullable = false)
    public UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertType type;

    @Column(nullable = false)
    public String title;

    @Column(name = "due_date")
    public LocalDate dueDate;

    @Column(name = "lead_days")
    public Integer leadDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public AlertStatus status;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "resolved_at")
    public Instant resolvedAt;

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

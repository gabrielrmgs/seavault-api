package br.dev.irontech.seavault.reports.domain;

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
@Table(name = "report_history")
public class ReportRecord {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReportType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ReportFormat format;

    @Column
    public String params;

    @Column(name = "generated_at", nullable = false)
    public Instant generatedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        if (this.generatedAt == null) {
            this.generatedAt = now;
        }
    }
}

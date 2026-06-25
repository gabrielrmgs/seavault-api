package br.dev.irontech.seavault.voyages.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "voyages")
public class Voyage {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "vessel_id")
    public UUID vesselId;

    @Column(name = "company_id")
    public UUID companyId;

    @Column(name = "navigation_type_id")
    public UUID navigationTypeId;

    @Column(name = "category_id")
    public UUID categoryId;

    @Column
    public String role;

    @Column(name = "embark_date", nullable = false)
    public LocalDate embarkDate;

    @Column(name = "disembark_date")
    public LocalDate disembarkDate;

    @Column(name = "embark_port")
    public String embarkPort;

    @Column(name = "disembark_port")
    public String disembarkPort;

    @Column(name = "calculated_days")
    public Integer calculatedDays;

    @Column(name = "computed_days")
    public Integer computedDays;

    @Column(name = "override_reason")
    public String overrideReason;

    @Column(name = "overridden_at")
    public Instant overriddenAt;

    @Column
    public String notes;

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

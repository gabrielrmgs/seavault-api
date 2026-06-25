package br.dev.irontech.seavault.courses.domain;

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
@Table(name = "courses")
public class Course {

    @Id
    @UuidGenerator
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(nullable = false)
    public String name;

    @Column(name = "catalog_course_id")
    public UUID catalogCourseId;

    @Column
    public String institution;

    @Column
    public String modality;

    @Column(name = "workload_hours")
    public Integer workloadHours;

    @Column(name = "start_date")
    public LocalDate startDate;

    @Column(name = "completion_date")
    public LocalDate completionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CourseStatus status;

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

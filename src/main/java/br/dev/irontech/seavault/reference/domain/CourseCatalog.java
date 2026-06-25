package br.dev.irontech.seavault.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ref_course_catalog")
public class CourseCatalog {

    @Id
    public UUID id;

    @Column(nullable = false)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(name = "workload_hours")
    public Integer workloadHours;
}

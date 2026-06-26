package br.dev.irontech.seavault.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ref_eligibility_rules")
public class EligibilityRule {

    @Id
    public UUID id;

    @Column(nullable = false)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(name = "target_category_id")
    public UUID targetCategoryId;

    @Column(name = "target_course_id")
    public UUID targetCourseId;
}

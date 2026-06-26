package br.dev.irontech.seavault.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ref_eligibility_requirements")
public class EligibilityRequirement {

    @Id
    public UUID id;

    @Column(name = "rule_id", nullable = false)
    public UUID ruleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type", nullable = false)
    public RequirementType requirementType;

    @Column(name = "required_course_id")
    public UUID requiredCourseId;

    @Column(name = "required_category_id")
    public UUID requiredCategoryId;

    @Column(name = "required_days")
    public Integer requiredDays;

    @Column(name = "display_order", nullable = false)
    public int displayOrder;
}

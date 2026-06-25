package br.dev.irontech.seavault.reference.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "ref_categories")
public class Category {

    @Id
    public UUID id;

    @Column(name = "group_id", nullable = false)
    public UUID groupId;

    @Column(nullable = false)
    public String code;

    @Column(nullable = false)
    public String name;

    @Column(name = "progression_order", nullable = false)
    public int progressionOrder;
}

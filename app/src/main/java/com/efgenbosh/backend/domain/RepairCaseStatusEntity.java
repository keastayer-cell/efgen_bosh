package com.efgenbosh.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "w_repair_case_status", schema = "work")
public class RepairCaseStatusEntity {
    @Id private Integer id;
    @Column(nullable = false, unique = true, length = 32) private String code;
    @Column(nullable = false, length = 120) private String label;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder;
    @Column(nullable = false) private boolean active = true;

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getLabel() { return label; }
    public Integer getSortOrder() { return sortOrder; }
    public boolean isActive() { return active; }
}

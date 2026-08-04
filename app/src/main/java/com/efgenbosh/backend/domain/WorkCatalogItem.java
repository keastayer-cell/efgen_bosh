package com.efgenbosh.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "w_work_catalog_item", schema = "work")
public class WorkCatalogItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private WorkCategory category;
    @Column(nullable = false) private String code;
    @Column(nullable = false) private String name;
    @Column(name = "default_unit", nullable = false, length = 40) private String defaultUnit = "н/ч";
    @Column(name = "norm_hours", nullable = false, precision = 10, scale = 2) private java.math.BigDecimal normHours = java.math.BigDecimal.ONE;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    public Long getId() { return id; }
    public WorkCategory getCategory() { return category; }
    public void setCategory(WorkCategory value) { category = value; }
    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String value) { defaultUnit = value; }
    public java.math.BigDecimal getNormHours() { return normHours; }
    public void setNormHours(java.math.BigDecimal value) { normHours = value == null ? java.math.BigDecimal.ONE : value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public Integer getSortOrder() { return sortOrder; }
}

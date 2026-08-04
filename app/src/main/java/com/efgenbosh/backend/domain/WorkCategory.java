package com.efgenbosh.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "w_work_category", schema = "work")
public class WorkCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public boolean isActive() { return active; }
    public Integer getSortOrder() { return sortOrder; }
}

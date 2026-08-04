package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_supplier", schema = "work")
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "legacy_id") private String legacyId;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private boolean active = true;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    public Long getId() { return id; }
    public String getLegacyId() { return legacyId; }
    public void setLegacyId(String value) { legacyId = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
}

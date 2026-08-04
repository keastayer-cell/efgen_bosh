package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_vehicle_alias", schema = "work")
public class VehicleAlias {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "source_name", nullable = false) private String sourceName;
    @Column(name = "normalized_latin_name", nullable = false) private String normalizedLatinName = "";
    @Column(nullable = false) private boolean active = true;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    public Long getId() { return id; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String value) { sourceName = value; }
    public String getNormalizedLatinName() { return normalizedLatinName; }
    public void setNormalizedLatinName(String value) { normalizedLatinName = value; }
    public boolean isActive() { return active; }
    public void setActive(boolean value) { active = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
}

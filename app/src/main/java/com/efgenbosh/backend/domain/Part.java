package com.efgenbosh.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "w_part", schema = "work")
public class Part {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "legacy_id", length = 255) private String legacyId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false) private Car car;
    @Column(nullable = false, length = 500) private String name;
    @Column(nullable = false, length = 255) private String article = "";
    @Column(name = "supplier_id") private Long supplierId;
    @Column(name = "expected_date") private LocalDate expectedDate;
    @Column(nullable = false) private boolean received;
    @Column(name = "received_at") private LocalDate receivedAt;
    @Column(name = "sort_order", nullable = false) private Integer sortOrder = 0;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;

    public Long getId() { return id; }
    public String getLegacyId() { return legacyId; }
    public void setLegacyId(String value) { legacyId = value; }
    public Car getCar() { return car; }
    public void setCar(Car value) { car = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getArticle() { return article; }
    public void setArticle(String value) { article = value; }
    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long value) { supplierId = value; }
    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate value) { expectedDate = value; }
    public boolean isReceived() { return received; }
    public void setReceived(boolean value) { received = value; }
    public LocalDate getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDate value) { receivedAt = value; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer value) { sortOrder = value; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void touch() { updatedAt = OffsetDateTime.now(); }
}

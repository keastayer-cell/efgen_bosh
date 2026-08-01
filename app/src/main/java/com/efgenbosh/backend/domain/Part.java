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
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "repair_case_id") private RepairCase repairCase;
    @Column(nullable = false, length = 500) private String name;
    @Column(nullable = false, length = 255) private String article = "";
    @Column(name = "catalog_number", nullable = false) private String catalogNumber = "";
    @Column(nullable = false) private String manufacturer = "";
    @Column(nullable = false, precision = 12, scale = 3) private java.math.BigDecimal quantity = java.math.BigDecimal.ONE;
    @Column(name = "ordered_at") private LocalDate orderedAt;
    @Column(nullable = false, length = 1000) private String comment = "";
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
    public RepairCase getRepairCase() { return repairCase; }
    public void setRepairCase(RepairCase value) { repairCase = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public String getArticle() { return article; }
    public void setArticle(String value) { article = value; }
    public String getCatalogNumber() { return catalogNumber; } public void setCatalogNumber(String value) { catalogNumber = value; }
    public String getManufacturer() { return manufacturer; } public void setManufacturer(String value) { manufacturer = value; }
    public java.math.BigDecimal getQuantity() { return quantity; } public void setQuantity(java.math.BigDecimal value) { quantity = value; }
    public LocalDate getOrderedAt() { return orderedAt; } public void setOrderedAt(LocalDate value) { orderedAt = value; }
    public String getComment() { return comment; } public void setComment(String value) { comment = value; }
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

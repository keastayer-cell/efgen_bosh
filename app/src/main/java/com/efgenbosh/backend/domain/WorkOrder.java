package com.efgenbosh.backend.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "w_work_order", schema = "work")
public class WorkOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id", nullable = false, unique = true) private Car car;
    @Column(nullable = false, length = 32) private String status = "DRAFT";
    @Column(name = "document_date", nullable = false) private LocalDate documentDate = LocalDate.now();
    @Column(nullable = false, length = 500) private String customer = "";
    @Column(name = "claim_number", nullable = false, length = 120) private String claimNumber = "";
    @Column(name = "vehicle_name", nullable = false) private String vehicleName = "";
    @Column(name = "registration_number", nullable = false, length = 32) private String registrationNumber = "";
    @Column(nullable = false, length = 32) private String vin = "";
    @Column(name = "created_by") private Long createdBy;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt = OffsetDateTime.now();
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt = OffsetDateTime.now();
    @Version @Column(nullable = false) private Long version;
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC") private List<WorkOrderLine> lines = new ArrayList<>();

    public Long getId() { return id; }
    public Car getCar() { return car; }
    public void setCar(Car value) { car = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDate getDocumentDate() { return documentDate; }
    public void setDocumentDate(LocalDate value) { documentDate = value; }
    public String getCustomer() { return customer; }
    public void setCustomer(String value) { customer = value; }
    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String value) { claimNumber = value; }
    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String value) { vehicleName = value; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String value) { registrationNumber = value; }
    public String getVin() { return vin; }
    public void setVin(String value) { vin = value; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long value) { createdBy = value; }
    public List<WorkOrderLine> getLines() { return lines; }
    public void replaceLines(List<WorkOrderLine> values) { lines.clear(); values.forEach(this::addLine); }
    public void addLine(WorkOrderLine line) { lines.add(line); line.setWorkOrder(this); }
    public BigDecimal total() { return lines.stream().map(WorkOrderLine::total).reduce(BigDecimal.ZERO, BigDecimal::add); }
    public void touch() { updatedAt = OffsetDateTime.now(); }
}
